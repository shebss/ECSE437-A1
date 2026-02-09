Program MyUniversityProgram {
    
    // Define courses
    Course ECSE250 "Introduction to Computer Science" {
        credits 3
        year 2
        term both
    }
    
    Course COMP250 "Introduction to Computer Science" {
        credits 3
        year 2
        term both
    }
    
    Course ECSE223 "Model-Based Programming" {
        credits 3
        year 2
        term fall
        prerequisites ECSE250
    }
    
    Course ECSE321 "Introduction to Software Engineering" {
        credits 3
        year 3
        term winter
        prerequisites ECSE250
    }
    
    Course ECSE439 "Model-Based Software Engineering" {
        credits 3
        year 4
        term fall
        prerequisites (ECSE223 OR ECSE321)
    }
    
    Course ECSE539 "Advanced Software Language Engineering" {
        credits 3
        year 5
        term fall
        prerequisites ECSE439
    }
    
    Course ECSE430 "Photonic Devices and Systems" {
        credits 3
        year 4
        term winter
        prerequisites (ECSE250 OR COMP250) AND (ECSE439 OR ECSE539)
        corequisites ECSE321
    }
    
    // Define students
    Student Alice {
        completed { ECSE250 COMP250 ECSE223 }
        maxCreditsPerTerm 15
    }
    
    Student Bob {
        completed { ECSE250 ECSE321 }
        maxCreditsPerTerm 12
    }
}
