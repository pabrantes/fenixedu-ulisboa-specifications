package org.fenixedu.ulisboa.specifications.ui.facade;

import javax.servlet.http.HttpServletRequest;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.ulisboa.specifications.ui.FenixeduUlisboaSpecificationsController;
import org.springframework.web.bind.annotation.RequestMapping;

import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.GenericChecksumRewriter;

@SpringFunctionality(app = FenixeduUlisboaSpecificationsController.class, title = "label.title.courseEnrolmentFacade")
@RequestMapping("courseEnrolmentFacade")
public class CourseEnrolmentFacade {

    @RequestMapping
    public String doRedirect(HttpServletRequest request) {
        String link = "/student/studentEnrollmentManagement.do?method=prepare";

        //request
        String injectChecksumInUrl =
                GenericChecksumRewriter.injectChecksumInUrl(request.getContextPath(), link, request.getSession());
        return "redirect:" + injectChecksumInUrl;
    }

}
