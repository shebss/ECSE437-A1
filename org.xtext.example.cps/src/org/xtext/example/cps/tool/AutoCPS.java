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

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.XtextResource;

public class AutoCPS {
	public static void main(String[] args) {
		
		// Set up for CPS model input
		Injector injector = new CPSStandaloneSetup().createInjectorAndDoEMFRegistration();
        XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
        
        // Set up for XMI output
        resourceSet.getResourceFactoryRegistry()
          .getExtensionToFactoryMap()
          .put("xmi", new XMIResourceFactoryImpl());
        
        resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);

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
        URI xmiURI = URI.createFileURI("data/program.xmi");
        Resource xmiResource = resourceSet.createResource(xmiURI);
        xmiResource.getContents().add(cps);
        try {
        	xmiResource.save(Collections.emptyMap());
        } catch (IOException e){
        	System.err.println("Failed to save data/program.xmi");
            e.printStackTrace();
        }
	}
	
	private static void generateSchedule(CoursePlanningSystem cps, Student student) {
		System.out.println("-------- Schedule for " + student.getName() + " --------");
		
		// Clear existing schedule
		student.getSchedule().clear();
		
		// Get courses
		List<Course> allCourses = cps.getCourses();
		List<Course> completedCourses = new ArrayList<>(student.getCompletedCourses());
		List<Course> remainingCourses = new ArrayList<>();
		
		
		for (Course c : allCourses) {
			if (!completedCourses.contains(c)) {
				remainingCourses.add(c);
			}
		}

	    if (remainingCourses.isEmpty()) {
	        System.out.println("No remaining courses to schedule.");
	        return;
	    }
		
		
		// Sort remaining courses by year
		remainingCourses.sort(Comparator.comparingInt(Course::getYear));
		
	    System.out.println("Completed courses resolved: " + 
		        completedCourses.stream().map(Course::getName).collect(Collectors.joining(", ")));

	    System.out.println("Remaining courses: " + 
	        remainingCourses.stream().map(Course::getName).collect(Collectors.joining(", ")) + "\n");

		// Select the courses student is eligible to take until all courses have been scheduled
		int termNum = 1;
		boolean isFallTerm = true;
		int emptyTermCount = 0;
	
		while (!remainingCourses.isEmpty()) {
			TermAssignment term = CpsFactory.eINSTANCE.createTermAssignment();
			term.setTermNumber(termNum);
			term.setSeason(isFallTerm ? TermSeason.FALL : TermSeason.WINTER);
			
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
			
			 if (!coursesScheduledThisTerm.isEmpty()) {
			        student.getSchedule().add(term);
			        emptyTermCount = 0;  // Reset counter
			        
			        System.out.println(term.getSeason() + " Term " + termNum + ":");
			        for (Course c : coursesScheduledThisTerm) {
			            System.out.println("  - " + c.getName() + " " + c.getCourseTitle()
			                    + " (" + c.getCredits() + " credits)");
			        }
			        System.out.println("  Total credits: " + numCreditsThisTerm + "\n");
			        
			        completedCourses.addAll(coursesScheduledThisTerm);
			    } else {
			        emptyTermCount++;
			    }
            
            // Go to next term
            if (!isFallTerm) {
            	termNum++;
            }
            
            isFallTerm = !isFallTerm;
            
            if (emptyTermCount >= 2) {
                System.err.println("Cannot schedule the following remaining courses:\n");
                for (Course c : remainingCourses) {
                    System.err.println("  - " + c.getName() + " " + c.getCourseTitle());
                }
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