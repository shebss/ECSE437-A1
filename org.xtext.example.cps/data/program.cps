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
        credits 4
        year 5
        term winter
        prerequisites (COMP206 OR COMP251) AND COMP302
        corequisites ECSE437
    }
    
    
// completed foundational courses
    Student hihi {
        completed { COMP206 COMP250 ECSE321 }
        maxCreditsPerTerm 15
    }

    // completed year 2 and some year 3
    Student haha {
        completed { COMP206 COMP250 ECSE321 COMP251 COMP302 COMP273 }
        maxCreditsPerTerm 12
    }

    // only advanced courses remaining
    Student hehe {
        completed { COMP206 COMP250 ECSE321 COMP251 COMP273 COMP302 COMP303 }
        maxCreditsPerTerm 9
    }

    // low credit limit
    Student hoho {
        completed { COMP206 COMP250 }
        maxCreditsPerTerm 3
    }
}
