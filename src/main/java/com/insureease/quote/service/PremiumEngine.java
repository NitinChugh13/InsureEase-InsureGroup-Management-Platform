package com.insureease.quote.service;

import com.insureease.common.exception.BusinessRuleException;
import com.insureease.product.entity.Plan;
import com.insureease.product.entity.Product;
import com.insureease.product.entity.RateRow;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Multi-product Strategy + Modifier Chain Premium Engine (BR-1, BR-12).
 * Supports Group Health (GMC), Group Term Life (GTL), and Group Personal Accident (GPA).
 */
@Component
public class PremiumEngine {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal THOUSAND = new BigDecimal("1000.00");

    public record MemberPricingResult(
            BigDecimal requestedSi,
            BigDecimal approvedSi,
            BigDecimal excessFclSi,
            String uwStatus,
            BigDecimal basePremium
    ) {
    }

    public MemberPricingResult priceSingleMember(Product product,
                                                 Plan plan,
                                                 List<RateRow> rateRows,
                                                 int age,
                                                 String gender,
                                                 String relation,
                                                 String occupationClass,
                                                 BigDecimal annualCtc,
                                                 BigDecimal inputSi,
                                                 String currentUwStatus) {
        String prodType = product.getProductType().toUpperCase();
        String normRelation = relation != null ? relation.toUpperCase() : "SELF";
        String normGender = gender != null ? gender.toUpperCase() : "ANY";
        String normOcc = occupationClass != null ? occupationClass.toUpperCase() : "CLASS_I";

        // 1. Determine Requested Sum Insured (Flat Slab vs Salary Multiple)
        BigDecimal requestedSi = inputSi != null ? inputSi : plan.getMinSumInsured();
        if ("SALARY_MULTIPLE".equalsIgnoreCase(plan.getSiBasis()) && annualCtc != null
                && plan.getSalaryMultiple() != null && annualCtc.compareTo(BigDecimal.ZERO) > 0) {
            requestedSi = annualCtc.multiply(plan.getSalaryMultiple()).setScale(2, RoundingMode.HALF_UP);
            if (requestedSi.compareTo(plan.getMaxSumInsured()) > 0) {
                requestedSi = plan.getMaxSumInsured();
            }
            if (requestedSi.compareTo(plan.getMinSumInsured()) < 0) {
                requestedSi = plan.getMinSumInsured();
            }
        }

        // 2. Apply Free Cover Limit (FCL) for Group Term Life (BR-12)
        BigDecimal approvedSi = requestedSi;
        BigDecimal excessFclSi = BigDecimal.ZERO;
        String uwStatus = currentUwStatus != null ? currentUwStatus : "AUTO_APPROVED";

        if ("LIFE".equals(prodType) && plan.getFreeCoverLimit() != null
                && requestedSi.compareTo(plan.getFreeCoverLimit()) > 0) {
            if ("APPROVED".equalsIgnoreCase(currentUwStatus)) {
                approvedSi = requestedSi;
                excessFclSi = BigDecimal.ZERO;
                uwStatus = "APPROVED";
            } else if ("DECLINED".equalsIgnoreCase(currentUwStatus)) {
                approvedSi = plan.getFreeCoverLimit();
                excessFclSi = BigDecimal.ZERO;
                uwStatus = "DECLINED_EXCESS_FCL";
            } else {
                approvedSi = plan.getFreeCoverLimit();
                excessFclSi = requestedSi.subtract(plan.getFreeCoverLimit());
                uwStatus = "MEDICAL_REQUIRED";
            }
        } else if (!"APPROVED".equalsIgnoreCase(uwStatus)) {
            uwStatus = "AUTO_APPROVED";
        }

        // 3. Match Rate Row according to Product Strategy
        RateRow matchedRow = findMatchingRateRow(rateRows, age, normGender, normRelation, normOcc, approvedSi);
        BigDecimal baseRate = matchedRow.getBaseRate();

        // 4. Calculate Member Base Premium based on Plan Rate Unit
        BigDecimal memberPremium;
        String rateUnit = plan.getRateUnit().toUpperCase();
        if ("PER_MILLE".equals(rateUnit)) {
            memberPremium = approvedSi.divide(THOUSAND, 6, RoundingMode.HALF_UP)
                    .multiply(baseRate)
                    .setScale(2, RoundingMode.HALF_UP);
        } else if ("PCT_OF_SI".equals(rateUnit)) {
            memberPremium = approvedSi.multiply(baseRate)
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        } else {
            // FLAT_AMOUNT (typical GMC slab rate)
            memberPremium = baseRate.setScale(2, RoundingMode.HALF_UP);
        }

        // 5. Apply Family Floater Dependent Discount for GMC (if FAMILY_FLOATER and dependent)
        if ("HEALTH".equals(prodType)
                && "FAMILY_FLOATER".equalsIgnoreCase(plan.getCoverBasis())
                && !"SELF".equals(normRelation)
                && plan.getFloaterDiscountPct() != null
                && plan.getFloaterDiscountPct().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal factor = BigDecimal.ONE.subtract(
                    plan.getFloaterDiscountPct().divide(HUNDRED, 6, RoundingMode.HALF_UP));
            memberPremium = memberPremium.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        }

        return new MemberPricingResult(requestedSi, approvedSi, excessFclSi, uwStatus, memberPremium);
    }

    public BigDecimal computeGroupSizeDiscountPct(int employeeCount) {
        if (employeeCount >= 1000) {
            return new BigDecimal("15.00");
        }
        if (employeeCount >= 200) {
            return new BigDecimal("10.00");
        }
        if (employeeCount >= 50) {
            return new BigDecimal("5.00");
        }
        return BigDecimal.ZERO;
    }

    /**
     * BR-1: Net Premium = Base * (1 + loadings - discounts), Total = Net * (1 + GST)
     */
    public record AggregatePremiumBreakdown(
            BigDecimal basePremium,
            BigDecimal groupDiscountPct,
            BigDecimal loadingPct,
            BigDecimal uwAdjustmentPct,
            BigDecimal netPremium,
            BigDecimal gstPct,
            BigDecimal gstAmount,
            BigDecimal totalPremium
    ) {
    }

    public AggregatePremiumBreakdown computeAggregate(BigDecimal sumMemberBasePremium,
                                                      int employeeCount,
                                                      BigDecimal loadingPct,
                                                      BigDecimal uwAdjustmentPct,
                                                      BigDecimal gstPct) {
        BigDecimal groupDiscountPct = computeGroupSizeDiscountPct(employeeCount);
        BigDecimal safeLoading = loadingPct != null ? loadingPct : BigDecimal.ZERO;
        BigDecimal safeUwAdj = uwAdjustmentPct != null ? uwAdjustmentPct : BigDecimal.ZERO;
        BigDecimal safeGstPct = gstPct != null ? gstPct : new BigDecimal("18.00");

        // Modifier = 1 + (loadingPct + uwAdjustmentPct - groupDiscountPct) / 100
        BigDecimal netPctDelta = safeLoading.add(safeUwAdj).subtract(groupDiscountPct);
        BigDecimal multiplier = BigDecimal.ONE.add(netPctDelta.divide(HUNDRED, 6, RoundingMode.HALF_UP));
        if (multiplier.compareTo(new BigDecimal("0.20")) < 0) {
            multiplier = new BigDecimal("0.20");
        }

        BigDecimal netPremium = sumMemberBasePremium.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gstAmount = netPremium.multiply(safeGstPct).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = netPremium.add(gstAmount).setScale(2, RoundingMode.HALF_UP);

        return new AggregatePremiumBreakdown(
                sumMemberBasePremium.setScale(2, RoundingMode.HALF_UP),
                groupDiscountPct,
                safeLoading,
                safeUwAdj,
                netPremium,
                safeGstPct,
                gstAmount,
                totalPremium
        );
    }

    private RateRow findMatchingRateRow(List<RateRow> rateRows,
                                        int age,
                                        String gender,
                                        String relation,
                                        String occupationClass,
                                        BigDecimal approvedSi) {
        return rateRows.stream()
                .filter(r -> age >= r.getAgeFrom() && age <= r.getAgeTo())
                .filter(r -> "ANY".equalsIgnoreCase(r.getGender()) || r.getGender().equalsIgnoreCase(gender))
                .filter(r -> "ANY".equalsIgnoreCase(r.getRelation()) || r.getRelation().equalsIgnoreCase(relation))
                .filter(r -> "ALL".equalsIgnoreCase(r.getOccupationClass()) || r.getOccupationClass().equalsIgnoreCase(occupationClass))
                .sorted(Comparator.comparingInt((RateRow r) -> specificityScore(r, gender, relation, occupationClass, approvedSi)).reversed())
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("BR-1",
                        "No matching rate row found for age=" + age + ", gender=" + gender
                                + ", relation=" + relation + ", occupationClass=" + occupationClass));
    }

    private int specificityScore(RateRow r, String gender, String relation, String occupationClass, BigDecimal si) {
        int score = 0;
        if (r.getGender().equalsIgnoreCase(gender)) {
            score += 4;
        }
        if (r.getRelation().equalsIgnoreCase(relation)) {
            score += 4;
        }
        if (r.getOccupationClass().equalsIgnoreCase(occupationClass)) {
            score += 8;
        }
        if (r.getSiSlab() != null && si != null) {
            if (r.getSiSlab().compareTo(si) == 0) {
                score += 16;
            } else if (r.getSiSlab().compareTo(si) >= 0) {
                score += 2;
            }
        }
        return score;
    }
}
