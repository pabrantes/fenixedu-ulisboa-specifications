/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: joao.roxo@qub-it.com 
 *               nuno.pinheiro@qub-it.com
 *
 * 
 * This file is part of FenixEdu Specifications.
 *
 * FenixEdu Specifications is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Specifications is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Specifications.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.ulisboa.specifications.ui.firstTimeCandidacy;

import static org.fenixedu.bennu.FenixeduUlisboaSpecificationsSpringConfiguration.BUNDLE;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.EnrolmentPeriod;
import org.fenixedu.academic.domain.EnrolmentPeriodInClassesCandidate;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.SchoolClass;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.curricularRules.CurricularRuleValidationType;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.predicate.AccessControl;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.ulisboa.specifications.domain.FirstYearRegistrationConfiguration;
import org.fenixedu.ulisboa.specifications.ui.FenixeduUlisboaSpecificationsBaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.GenericChecksumRewriter;
import pt.ist.fenixframework.Atomic;
import edu.emory.mathcs.backport.java.util.Collections;

@BennuSpringController(value = FirstTimeCandidacyController.class)
@RequestMapping(ScheduleClassesController.CONTROLLER_URL)
public class ScheduleClassesController extends FenixeduUlisboaSpecificationsBaseController {

    public static final String CONTROLLER_URL = "/fenixedu-ulisboa-specifications/firsttimecandidacy/scheduleclasses";

    Logger logger = LoggerFactory.getLogger(ScheduleClassesController.class);

    @RequestMapping(value = "/back", method = RequestMethod.GET)
    public String back(Model model, RedirectAttributes redirectAttributes) {
        return redirect(ShowSelectedCoursesController.CONTROLLER_URL, model, redirectAttributes);
    }

    @RequestMapping
    public String scheduleclasses(Model model, RedirectAttributes redirectAttributes) {
        if (!FirstTimeCandidacyController.isPeriodOpen()) {
            return redirect(FirstTimeCandidacyController.CONTROLLER_URL, model, redirectAttributes);
        }

        if (shouldBeSkipped()) {
            Registration registration = FirstTimeCandidacyController.getCandidacy().getRegistration();
            ExecutionSemester executionSemester = ExecutionYear.readCurrentExecutionYear().getFirstExecutionPeriod();
            associateShiftsFor(registration.getStudentCurricularPlan(executionSemester));
            return scheduleclassesToContinue(model, redirectAttributes);
        }

        Registration registration = FirstTimeCandidacyController.getCandidacy().getRegistration();
        model.addAttribute("hasAnnualShifts", hasAnnualShifts(registration));

        addInfoMessage(BundleUtil.getString(BUNDLE, "label.firstTimeCandidacy.scheduleClasses.info"), model);
        return "fenixedu-ulisboa-specifications/firsttimecandidacy/scheduleclasses";
    }

    public static boolean shouldBeSkipped() {
        Degree degree = FirstTimeCandidacyController.getCandidacy().getDegreeCurricularPlan().getDegree();
        if (degree.getFirstYearRegistrationConfiguration() == null) {
            return true;
        }
        if (!degree.getFirstYearRegistrationConfiguration().getRequiresClassesEnrolment()
                && !degree.getFirstYearRegistrationConfiguration().getRequiresShiftsEnrolment()) {
            return true;
        }
        return false;
    }

    @RequestMapping(value = "/openshiftenrollments")
    public String scheduleclassesToOpenShiftEnrollments(Model model, RedirectAttributes redirectAttributes) {
        if (!FirstTimeCandidacyController.isPeriodOpen()) {
            return redirect(FirstTimeCandidacyController.CONTROLLER_URL, model, redirectAttributes);
        }
        ExecutionSemester executionSemester = ExecutionSemester.readActualExecutionSemester();

        Registration registration = FirstTimeCandidacyController.getCandidacy().getRegistration();
        String link = "/student/schoolClassStudentEnrollment.do?method=prepare&workflowRegistrationOid=%s";
        String format = String.format(link, registration.getExternalId());

        Degree degree = registration.getDegree();
        FirstYearRegistrationConfiguration firstYearRegistrationConfiguration = degree.getFirstYearRegistrationConfiguration();
        if (firstYearRegistrationConfiguration.getRequiresClassesEnrolment()) {
            String injectChecksumInUrl =
                    GenericChecksumRewriter.injectChecksumInUrl(request.getContextPath(), format, request.getSession());
            return redirect(injectChecksumInUrl, model, redirectAttributes);
        }
        if (firstYearRegistrationConfiguration.getRequiresShiftsEnrolment()) {
            return redirect("/student/shiftEnrolment/switchEnrolmentPeriod/" + registration.getExternalId() + "/"
                    + getEnrolmentPeriodForSemester(executionSemester, registration).getExternalId(), model, redirectAttributes);
        }
        throw new RuntimeException("No classes or course enrolment for current degree");
    }

    private EnrolmentPeriod getEnrolmentPeriodForSemester(ExecutionSemester executionSemester, Registration registration) {
        return registration
                .getDegree()
                .getMostRecentDegreeCurricularPlan()
                .getEnrolmentPeriodsSet()
                .stream()
                .filter(ep -> ep instanceof EnrolmentPeriodInClassesCandidate && ep.getExecutionPeriod() == executionSemester)
                .findAny()
                .orElseThrow(
                        () -> new RuntimeException(
                                "You need an open period for classes/shifts for candidates to enable shift enrolmentstude"));
    }

    @RequestMapping(value = "/continue")
    public String scheduleclassesToContinue(Model model, RedirectAttributes redirectAttributes) {
        if (!FirstTimeCandidacyController.isPeriodOpen()) {
            return redirect(FirstTimeCandidacyController.CONTROLLER_URL, model, redirectAttributes);
        }
        return redirect(ShowScheduledClassesController.CONTROLLER_URL, model, redirectAttributes);
    }

    public void associateShiftsFor(final StudentCurricularPlan studentCurricularPlan) {
        Collection<ExecutionSemester> executionSemesters = null;
        if (!studentCurricularPlan.getStudent().getShiftsFor(ExecutionSemester.readActualExecutionSemester()).isEmpty()) {
            return;
        }
        if (hasAnnualShifts(studentCurricularPlan)) {
            executionSemesters = ExecutionYear.readCurrentExecutionYear().getExecutionPeriodsSet();
        } else {
            executionSemesters = Collections.singleton(ExecutionYear.readCurrentExecutionYear().getFirstExecutionPeriod());
        }
        Optional<SchoolClass> firstUnfilledClass =
                readFirstUnfilledClass(studentCurricularPlan.getRegistration(), executionSemesters);
        if (firstUnfilledClass.isPresent()) {
            logger.warn("Registering student " + studentCurricularPlan.getPerson().getUsername() + " to class "
                    + firstUnfilledClass.get().getNome());
            enrolOnShifts(firstUnfilledClass.get(), studentCurricularPlan.getRegistration());
        } else {
            logger.warn("No classes present. Skipping classes allocation for " + studentCurricularPlan.getPerson().getUsername()
                    + ". If this is expected, ignore this message.");
        }
    }

    private Optional<SchoolClass> readFirstUnfilledClass(Registration registration,
            final Collection<ExecutionSemester> executionSemesters) {
        ExecutionDegree executionDegree =
                registration.getDegree().getExecutionDegreesForExecutionYear(ExecutionYear.readCurrentExecutionYear()).iterator()
                        .next();
        Optional<SchoolClass> findFirst =
                executionDegree.getSchoolClassesSet().stream()
                        .filter(sc -> sc.getAnoCurricular().equals(1) && executionSemesters.contains(sc.getExecutionPeriod()))
                        .sorted(new MostFilledFreeClass()).findFirst();
        if (findFirst.isPresent()) {
            int sc1NumOfEnrolledStudents = getNumberOfEnrolledStudents(findFirst.get());
            int sc1AvailableSlots = avaliableSlots(findFirst.get(), sc1NumOfEnrolledStudents);
            if (sc1AvailableSlots == 0) {
                logger.error("No shifts available! Person " + AccessControl.getPerson().getName()
                        + " will have no assigned class.");
                return Optional.empty();
            }
        }
        return findFirst;
    }

    @Atomic
    protected void enrolOnShifts(final SchoolClass schoolClass, final Registration registration) {
        if (schoolClass == null) {
            throw new DomainException("error.RegistrationOperation.avaliable.schoolClass.not.found");
        }

        registration.replaceSchoolClass(schoolClass, schoolClass.getExecutionPeriod());
    }

    class MostFilledFreeClass implements Comparator<SchoolClass> {
        // Return at the beggining the course which is the most filled, but still has space
        // This allows a "fill first" kind of school class scheduling

        // Expected behaviour: 
        // Case 1: In the first iteration, all the conditions are false, the classes will be ordered by name
        // Case 2: In the second iteration the first element will always have its condition as true and all the others will have their conditions as false
        // Case 3: When the class is filled, its conditions will also be false, so it will stop being the first. To avoid other resolution by name, we force availability == 0 classes to go to the end of the list
        @Override
        public int compare(SchoolClass sc1, SchoolClass sc2) {
            int sc1NumOfEnrolledStudents = getNumberOfEnrolledStudents(sc1);
            int sc1AvailableSlots = avaliableSlots(sc1, sc1NumOfEnrolledStudents);
            int sc2NumOfEnrolledStudents = getNumberOfEnrolledStudents(sc2);
            int sc2AvailableSlots = avaliableSlots(sc2, sc2NumOfEnrolledStudents);

            //Case 2
            if (sc1NumOfEnrolledStudents > 0 && sc1AvailableSlots > 0) {
                return -1;
            }
            if (sc2NumOfEnrolledStudents > 0 && sc2AvailableSlots > 0) {
                return 1;
            }

            //Case 3
            if (sc1AvailableSlots == 0) {
                return 1;
            }
            if (sc2AvailableSlots == 0) {
                return -1;
            }

            //Case 1 
            return SchoolClass.COMPARATOR_BY_NAME.compare(sc1, sc2);
        }
    }

    //We are only interested in shifts which do not have more than one class (ignore shared shifts)
    Predicate<? super Shift> shiftsWithOneClass = s -> s.getAssociatedClassesSet().size() == 1;

    private int avaliableSlots(SchoolClass schoolClass, int sc1NumOfEnrolledStudents) {
        return schoolClass.getAssociatedShiftsSet().stream().filter(shiftsWithOneClass).mapToInt(shift -> shift.getLotacao())
                .min().orElse(0)
                - sc1NumOfEnrolledStudents;
    }

    private Integer getNumberOfEnrolledStudents(SchoolClass schoolClass) {
        return schoolClass.getAssociatedShiftsSet().stream().filter(shiftsWithOneClass)
                .map(shift -> shift.getStudentsSet().size()).findAny().orElse(0);
    }

    boolean hasAnnualShifts(StudentCurricularPlan studentCurricularPlan) {
        return studentCurricularPlan.getDegreeCurricularPlan().getCurricularRuleValidationType() == CurricularRuleValidationType.YEAR;
    }

    boolean hasAnnualShifts(Registration registration) {
        return hasAnnualShifts(registration.getStudentCurricularPlan(ExecutionYear.readCurrentExecutionYear()
                .getFirstExecutionPeriod()));
    }

}
