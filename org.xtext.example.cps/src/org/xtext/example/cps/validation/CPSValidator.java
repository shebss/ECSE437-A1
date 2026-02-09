package org.xtext.example.cps.validation;

import org.eclipse.xtext.validation.Check;
import org.xtext.example.cps.cps.Course;
import org.xtext.example.cps.cps.CpsPackage;
import org.xtext.example.cps.cps.Student;


public class CPSValidator extends AbstractCPSValidator {
	
	public static final String INVALID_CREDITS = "invalidCredits";
	public static final String INVALID_YEAR = "invalidYear";
	public static final String INVALID_MAX_CREDITS_PER_TERM = "invalidMaxCreditsPerTerm";
	
	@Check
	public void checkCourseCredits(Course course) {
		if (course.getCredits() > 4) {
			error("Course credits must be between 0 and 4 (inclusive). Current value: " + course.getCredits(),
					CpsPackage.Literals.COURSE__CREDITS,
					INVALID_CREDITS);
		}
	}
	
	@Check
	public void checkCourseYear(Course course) {
		if (course.getYear() < 1 || course.getYear() > 5) {
			error("Course year must be between 1 and 5 (inclusive). Current value: " + course.getYear(),
					CpsPackage.Literals.COURSE__YEAR,
					INVALID_YEAR);
		}
	}
	
	@Check
	public void checkMaxCreditsPerTerm(Student student) {
		if (student.getMaxCreditsPerTerm() > 22) {
			error("Maximum credits per term cannot exceed 22. Current value: " + student.getMaxCreditsPerTerm(),
					CpsPackage.Literals.STUDENT__MAX_CREDITS_PER_TERM,
					INVALID_MAX_CREDITS_PER_TERM);
		}
	}
}