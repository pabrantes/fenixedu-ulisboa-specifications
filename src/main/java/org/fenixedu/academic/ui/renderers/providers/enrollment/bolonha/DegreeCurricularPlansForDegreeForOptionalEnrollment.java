package org.fenixedu.academic.ui.renderers.providers.enrollment.bolonha;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.curricularRules.executors.ruleExecutors.AnyCurricularCourseExceptionsExecutorLogic;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.dto.student.enrollment.bolonha.BolonhaStudentOptionalEnrollmentBean;
import org.fenixedu.ulisboa.specifications.domain.curricularRules.AnyCurricularCourseExceptionsConfiguration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DegreeCurricularPlansForDegreeForOptionalEnrollment extends DegreeCurricularPlansForDegree {

    @Override
    public Object provide(Object source, Object currentValue) {
        final List<DegreeCurricularPlan> result = Lists.newArrayList();

        if (source instanceof BolonhaStudentOptionalEnrollmentBean) {

            final BolonhaStudentOptionalEnrollmentBean bean = (BolonhaStudentOptionalEnrollmentBean) source;
            result.addAll(getDegreeCurricularPlans(bean));

            final DegreeCurricularPlan current = (DegreeCurricularPlan) currentValue;
            if (!result.contains(current)) {
                bean.setDegreeCurricularPlan(null);
            }
        }

        Collections.sort(result, DegreeCurricularPlan.COMPARATOR_BY_NAME);
        return result;
    }

    static private Set<DegreeCurricularPlan> getDegreeCurricularPlans(final BolonhaStudentOptionalEnrollmentBean bean) {
        final Set<DegreeCurricularPlan> result = Sets.newHashSet();

        final DegreeType degreeType = bean.getDegreeType();
        final Degree degree = bean.getDegree();
        if (degreeType != null && degree != null) {

            if (degreeType != degree.getDegreeType()) {
                bean.setDegree(null);
                bean.setDegreeType(null);

            } else {

                for (final DegreeCurricularPlan iter : degree.getDegreeCurricularPlansSet()) {
                    if (iter.isActive() && iter.hasExecutionDegreeFor(bean.getExecutionYear())) {
                        result.add(iter);
                    }
                }
            }
        }

        return result;
    }

    static public Set<String> getAnyCurricularCourseExceptionsOIDs(final BolonhaStudentOptionalEnrollmentBean bean) {
        final Set<String> result = Sets.newHashSet();

        final AnyCurricularCourseExceptionsConfiguration configuration = AnyCurricularCourseExceptionsConfiguration.getInstance();
        final DegreeCurricularPlan chosenDegreeCurricularPlan = bean.getDegreeCurricularPlan();

        if (chosenDegreeCurricularPlan != null && configuration != null) {

            for (final CompetenceCourse competenceCourse : configuration.getCompetenceCoursesSet()) {

                // just to avoid a JSP with an array with 500 OIDs
                final CurricularCourse curricularCourse = competenceCourse.getCurricularCourse(chosenDegreeCurricularPlan);
                if (curricularCourse != null) {

                    if (AnyCurricularCourseExceptionsExecutorLogic.isException(competenceCourse, chosenDegreeCurricularPlan,
                            bean.getStudentCurricularPlan())) {

                        result.add(curricularCourse.getExternalId());
                    }
                }
            }
        }

        return result;
    }

}
