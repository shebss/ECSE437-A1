package org.xtext.example.cps.tool;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import org.xtext.example.cps.CPSStandaloneSetup;
import org.xtext.example.cps.cps.*;

public class AutoCPS {
	public static void main(String[] args) {
		
		// Set up for CPS model input
		Injector injector = new CPSStandaloneSetup().createInjectorAndDoEMFRegistration();
        XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);

        // Set up for XMI output
        resourceSet.getResourceFactoryRegistry()
          .getExtensionToFactoryMap()
          .put("xmi", new XMIResourceFactoryImpl());

        // Load CPS model
        URI modelURI = URI.createFileURI("data/program.cps");
        Resource model = resourceSet.getResource(modelURI, true);
        try {
        	model.load(Collections.emptyMap());
        } catch (IOException e) {
        	System.err.println("Failed to load data/program.cps");
            e.printStackTrace();
            return;
        }

        if (!model.getErrors().isEmpty()) {
            System.err.println("program.cps contains errors:");
            model.getErrors().forEach(e -> System.err.println(" - " + e));
            return;
        }

        CoursePlanningSystem cps = (CoursePlanningSystem) model.getContents().get(0);

        // Generate schedule for each student
        for (Student s : cps.getStudents()) {
            generateSchedule(cps, s);
        }

        // Save updated model as XMI
        // TODO: check how to save as per assignment instructions
        URI xmiURI = URI.createFileURI("data/program.xmi");
        Resource xmiResource = resourceSet.createResource(xmiURI);
        xmiResource.getContents().add(cps);
        try {
        	xmiResource.save(Collections.emptyMap());
        	System.out.println("Successfully saved model to data/program.xmi");
        } catch (IOException e){
        	System.err.println("Failed to save data/program.xmi");
            e.printStackTrace();
        }
	}
	
	private static void generateSchedule(CoursePlanningSystem cps, Student student) {
		System.out.println("Generating schedule for " + student.getName() + "...");
		
		// Clear existing schedule
		student.getSchedule().clear();
		
		// Get courses
//		List<Course> allCourses = cps.getCourses();
//		List<Course> completedCourses = new ArrayList<>(student.getCompletedCourses());
//		List<Course> remainingCourses = new ArrayList<>();
//		
//		
//		for (Course c : allCourses) {
//			if (!completedCourses.contains(c)) {
//				remainingCourses.add(c);
//			}
//		}
		Map<String, Course> courseByNumber = new HashMap<>();
	    for (Course c : cps.getCourses()) {
	    	System.out.println("Course: " + c);
	    	System.out.println("Course num: " + c.getCourseNumber());
	        if (c.getCourseNumber() != null) {
	            courseByNumber.put(c.getCourseNumber(), c);
	        } else {
	            System.err.println("Warning: Course without courseNumber found");
	        }
	    }

	    // Resolve completed courses to real instances
	    List<Course> completedCourses = new ArrayList<>();
	    for (Course completedRef : student.getCompletedCourses()) {
	        String num = completedRef.getCourseNumber();
	        System.out.println("Completed num: " + num);
	        Course realCourse = courseByNumber.get(num);
	        if (realCourse != null) {
	            completedCourses.add(realCourse);
	        } else {
	            System.err.println("Warning: Completed course not found in program: " + num);
	        }
	    }

	    // Get remaining courses
	    List<Course> remainingCourses = new ArrayList<>();
	    for (Course c : cps.getCourses()) {
	        if (!completedCourses.contains(c)) {          // ← now this works (same objects)
	            remainingCourses.add(c);
	        }
	    }

	    System.out.println("Completed courses resolved: " + 
	        completedCourses.stream().map(Course::getCourseNumber).collect(Collectors.joining(", ")));

	    System.out.println("Remaining courses: " + 
	        remainingCourses.stream().map(Course::getCourseNumber).collect(Collectors.joining(", ")));

	    if (remainingCourses.isEmpty()) {
	        System.out.println("No remaining courses to schedule.");
	        return;   // early exit is nicer here
	    }
		
		
		if (remainingCourses.isEmpty()) {
			System.out.println("No remaining courses to schedule.");
		}
		
		// Sort remaining courses by year
		remainingCourses.sort(Comparator.comparingInt(Course::getYear));
		
		// Select the courses student is eligible to take
		// until all courses have been scheduled
		int termNum = 1;
		boolean isFallTerm = true;
		
		while (!remainingCourses.isEmpty()) {
			TermAssignment term = CpsFactory.eINSTANCE.createTermAssignment();
			term.setTermNumber(termNum);
			term.setSeason(isFallTerm ? TermSeason.FALL : TermSeason.WINTER);
			System.out.println(term.getSeason() + " Term " + termNum + ":");
			
			int numCreditsThisTerm = 0;
			List<Course> coursesScheduledThisTerm = new ArrayList<>();
			
			Iterator<Course> iterator = remainingCourses.iterator();
			while (iterator.hasNext()) {
				Course course = iterator.next();
				
				if (isSchedulable(course, isFallTerm, completedCourses, coursesScheduledThisTerm,
						numCreditsThisTerm, student.getMaxCreditsPerTerm())) {
					term.getAssignedCourses().add(course);
					coursesScheduledThisTerm.add(course);
					numCreditsThisTerm += course.getCredits();
					iterator.remove();
				}
			}
			
			if (coursesScheduledThisTerm.isEmpty()) {
				System.out.println("No courses can be scheduled for this term.");
			}
			
			// Add term to student's schedule
			student.getSchedule().add(term);
			
			// Print term
            for (Course c : coursesScheduledThisTerm) {
                System.out.println(c.getCourseNumber() + " - " + c.getName()
                        + " (" + c.getCredits() + " credits)");
            }
            System.out.println("Total credits: " + numCreditsThisTerm);
            
            // Mark scheduled courses as completed
            completedCourses.addAll(coursesScheduledThisTerm);
            
            // Go to next term
            if (!isFallTerm) {
            	termNum++;
            }
            
            isFallTerm = !isFallTerm;
            
            // If we reach more than 10 terms (i.e. 10 years), exit loop
            if (termNum > 10) {
            	System.err.println("Cannot schedule more than 10 years of terms.");
            	break;
            }
		}
	}
	
	private static boolean isSchedulable(Course course,
            boolean isFallTerm,
            List<Course> completedCourses,
            List<Course> coursesScheduledThisTerm,
            int numCreditsThisTerm,
            int maxCredits) {
		
		// Check credit limit
		if (numCreditsThisTerm + course.getCredits() > maxCredits) {
			return false;
		}

		// Check season
		Term courseTerm = course.getTerm();
		if (courseTerm == Term.FALL && !isFallTerm) return false;
		if (courseTerm == Term.WINTER && isFallTerm) return false;
		
		// Check if prerequisites are fulfilled
		if (course.getPrerequisites() != null) {
            if (!arePrereqsFulfilled(course.getPrerequisites(), completedCourses)) {
                return false;
            }
        }

		// Check if corequisites are completed or are already in this term
		if (course.getCorequisites() != null) {
			List<Course> takenCourses = new ArrayList<>(completedCourses);
			takenCourses.addAll(coursesScheduledThisTerm);
			if (!arePrereqsFulfilled(course.getCorequisites(), takenCourses)) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean arePrereqsFulfilled(Prerequisite prereq, List<Course> completedCourses) {

        if (prereq instanceof CourseReference) {
            CourseReference ref = (CourseReference) prereq;
            return completedCourses.contains(ref.getCourse());
        }

        if (prereq instanceof AndPrerequisite) {
            AndPrerequisite and = (AndPrerequisite) prereq;
            return arePrereqsFulfilled(and.getLeft(), completedCourses)
                    && arePrereqsFulfilled(and.getRight(), completedCourses);
        }

        if (prereq instanceof OrPrerequisite) {
            OrPrerequisite or = (OrPrerequisite) prereq;
            return arePrereqsFulfilled(or.getLeft(), completedCourses)
                    || arePrereqsFulfilled(or.getRight(), completedCourses);
        }

        return false;
    }
}