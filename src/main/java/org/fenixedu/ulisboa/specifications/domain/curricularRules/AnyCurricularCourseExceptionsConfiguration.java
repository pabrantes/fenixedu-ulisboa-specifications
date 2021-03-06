package org.fenixedu.ulisboa.specifications.domain.curricularRules;

import org.fenixedu.ulisboa.specifications.domain.ULisboaSpecificationsRoot;

import pt.ist.fenixframework.Atomic;

public class AnyCurricularCourseExceptionsConfiguration extends AnyCurricularCourseExceptionsConfiguration_Base {
    
    protected AnyCurricularCourseExceptionsConfiguration() {
        super();
    }
    
    public static void init() {
        if (getInstance() != null) {
            return;
        }
        makeInstance();
    }

    private static AnyCurricularCourseExceptionsConfiguration instance;

    public static AnyCurricularCourseExceptionsConfiguration getInstance() {
        if (instance == null) {
            instance = ULisboaSpecificationsRoot.getInstance().getAnyCurricularCourseExceptionsConfiguration();
        }
        return instance;
    }

    @Atomic
    private static AnyCurricularCourseExceptionsConfiguration makeInstance() {
        final AnyCurricularCourseExceptionsConfiguration result = new AnyCurricularCourseExceptionsConfiguration();
        result.setULisboaSpecificationsRoot(ULisboaSpecificationsRoot.getInstance());
        return result;
    }

}
