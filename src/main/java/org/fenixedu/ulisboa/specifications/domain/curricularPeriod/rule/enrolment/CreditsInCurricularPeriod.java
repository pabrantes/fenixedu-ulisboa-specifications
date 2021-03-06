package org.fenixedu.ulisboa.specifications.domain.curricularPeriod.rule.enrolment;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.curricularPeriod.CurricularPeriod;
import org.fenixedu.academic.domain.curricularRules.executors.RuleResult;
import org.fenixedu.academic.domain.enrolment.EnrolmentContext;
import org.fenixedu.academic.domain.enrolment.IDegreeModuleToEvaluate;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.ulisboa.specifications.domain.curricularPeriod.CurricularPeriodConfiguration;
import org.fenixedu.ulisboa.specifications.domain.services.CurricularPeriodServices;

import pt.ist.fenixframework.Atomic;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CreditsInCurricularPeriod extends CreditsInCurricularPeriod_Base {

    protected CreditsInCurricularPeriod() {
        super();
    }

    static public CreditsInCurricularPeriod createForSemester(final CurricularPeriodConfiguration configuration,
            final BigDecimal credits, final Integer semester, final Integer year) {

        return create(configuration, credits, semester, year, year);
    }

    static public CreditsInCurricularPeriod createForYear(final CurricularPeriodConfiguration configuration,
            final BigDecimal credits, final Integer year) {

        return createForYearInterval(configuration, credits, year, year);
    }

    static public CreditsInCurricularPeriod createForYearInterval(final CurricularPeriodConfiguration configuration,
            final BigDecimal credits, final Integer yearMin, final Integer yearMax) {

        return create(configuration, credits, (Integer) null /* semester */, yearMin, yearMax);
    }

    @Atomic
    static private CreditsInCurricularPeriod create(final CurricularPeriodConfiguration configuration, final BigDecimal credits,
            final Integer semester, final Integer yearMin, final Integer yearMax) {

        final CreditsInCurricularPeriod result = new CreditsInCurricularPeriod();
        result.init(configuration, credits, semester, yearMin, yearMax);
        return result;
    }

    private void init(final CurricularPeriodConfiguration configuration, final BigDecimal credits, final Integer semester,
            final Integer yearMin, final Integer yearMax) {

        super.init(configuration, credits, semester);
        setYearMin(yearMin);
        setYearMax(yearMax);
        checkRules();
    }

    private void checkRules() {
        if (getYearMin() == null) {
            throw new DomainException("error." + this.getClass().getSimpleName() + ".yearMin.required");
        }
    }

    private boolean isForYear() {
        return getYearMin() != null && getYearMax() != null && getYearMin().intValue() == getYearMax().intValue();
    }

    @Override
    public String getLabel() {
        if (getSemester() != null) {
            return BundleUtil.getString(MODULE_BUNDLE, "label." + this.getClass().getSimpleName() + ".semester", getCredits()
                    .toString(), getSemester().toString(), getYearMin().toString());

        } else if (isForYear()) {
            return BundleUtil.getString(MODULE_BUNDLE, "label." + this.getClass().getSimpleName() + ".year", getCredits()
                    .toString(), getYearMin().toString());

        } else {
            return BundleUtil.getString(MODULE_BUNDLE, "label." + this.getClass().getSimpleName(), getCredits().toString(),
                    getYearMin().toString(), getYearMax().toString());
        }
    }

    @Override
    public RuleResult execute(final EnrolmentContext enrolmentContext) {
        final Set<CurricularPeriod> configured = Sets.newHashSet();

        final DegreeCurricularPlan dcp = getDegreeCurricularPlan();

        for (int i = getYearMin(); i <= getYearMax(); i++) {
            final CurricularPeriod curricularPeriod = CurricularPeriodServices.getCurricularPeriod(dcp, i, getSemester());

            if (curricularPeriod == null) {
                return createFalseConfiguration();
            } else {
                configured.add(curricularPeriod);
            }
        }

        final BigDecimal total = getCreditsEnroledAndEnroling(enrolmentContext, configured);

        return total.compareTo(getCredits()) <= 0 ? createTrue() : createFalseLabelled(total);
    }

    private BigDecimal getCreditsEnroledAndEnroling(final EnrolmentContext enrolmentContext,
            final Set<CurricularPeriod> configured) {

        BigDecimal result = BigDecimal.ZERO;

        final Map<CurricularPeriod, BigDecimal> curricularPeriodCredits = mapYearCredits(enrolmentContext);
        final Set<CurricularPeriod> toInspect = configured;

        for (final CurricularPeriod iter : toInspect) {
            final BigDecimal credits = curricularPeriodCredits.get(iter);
            if (credits != null) {
                result = result.add(credits);
            }
        }

        return result;
    }

    private Map<CurricularPeriod, BigDecimal> mapYearCredits(final EnrolmentContext enrolmentContext) {
        final Map<CurricularPeriod, BigDecimal> result = Maps.newHashMap();

        final DegreeCurricularPlan dcp = enrolmentContext.getStudentCurricularPlan().getDegreeCurricularPlan();
        final ExecutionYear executionYear = enrolmentContext.getExecutionPeriod().getExecutionYear();

        for (final IDegreeModuleToEvaluate iter : getEnroledAndEnroling(enrolmentContext)) {

            final int year = iter.getContext().getCurricularYear();
            final CurricularPeriod curricularPeriod = CurricularPeriodServices.getCurricularPeriod(dcp, year, getSemester());

            if (curricularPeriod != null) {

                final BigDecimal credits =
                        BigDecimal.valueOf(getSemester() != null ? iter.getAccumulatedEctsCredits(executionYear
                                .getExecutionSemesterFor(getSemester())) : iter.getEctsCredits());

                CurricularPeriodServices.addYearCredits(result, curricularPeriod, credits);
            }
        }

        return result;
    }

}
