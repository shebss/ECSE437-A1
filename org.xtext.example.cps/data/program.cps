Program MyUniversityProgram {
    

    
    Course COMP250 "Introduction to Computer Science" {
        credits 3
        year 2
        term both
    }
    
    Course ECSE321 "Introduction to Software Engineering" {
        credits 3
        year 2
        term both
    }
    
    Course COMP206 "Introduction to Software Systems" {
        credits 3
        year 2
        term both
    }
    
    Course COMP273 "Introduction to Computer Systems" {
        credits 3
        year 2
        term winter
        corequisites COMP206
    }
    
    Course COMP251 "Algorithms and Data Structures" {
        credits 3
        year 3
        term both
        prerequisites COMP250
    }
    
    Course COMP302 "Programming Languages and Paradigms" {
        credits 3
        year 3
        term both
        prerequisites COMP250
    }
    
    Course COMP303 "Software Design" {
        credits 3
        year 4
        term fall
        prerequisites COMP206 AND COMP250
    }
    
    Course ECSE437 "Software Delivery" {
        credits 3
        year 5
        term winter
        prerequisites COMP303 OR ECSE321
    }
    
    Course COMP421 "Database Systems" {
        credits 3
        year 5
        term winter
        prerequisites (COMP206 OR COMP251) AND COMP302
        corequisites ECSE437
    }
    
    // students
    Student hihi {
        completed { COMP250, COMP251, COMP206 }
        maxCreditsPerTerm 15
    }
    
    Student haha {
        completed { COMP250, COMP251, COMP206, COMP302 }
        maxCreditsPerTerm 12
    }
}
