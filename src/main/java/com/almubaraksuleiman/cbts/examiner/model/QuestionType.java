// Package declaration - organizes classes within the model package
package com.almubaraksuleiman.cbts.examiner.model;

/**
 * Enumeration representing different types of questions supported in the
 * Computer-Based Test (CBT) system.

 * This enum defines the various question formats that can be used in tests,
 * each with different presentation, response, and grading requirements.

 * The enum values are stored as strings in the database using JPA's
 * @ Enumerated(EnumType.STRING) annotation on the Question entity.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
public enum QuestionType {

    /**
     * Multiple Choice question type.

     * Characteristics:
     * - Presents a question with several predefined answer options
     * - Student selects exactly one correct answer from the choices
     * - Typically uses radio buttons for selection
     * - Automated grading is straightforward (exact match checking)
     * - Most common question type in objective testing

     * Example: "What is the capital of France?" with options: [London, Paris, Berlin, Madrid]
     * Correct answer: "Paris"
     */
    MULTIPLE_CHOICE,

    /**
     * Multiple Select question type (also known as Multiple Response).

     * Characteristics:
     * - Presents a question with several predefined answer options
     * - Student selects one or more correct answers from the choices
     * - Typically uses checkboxes for selection
     * - Requires all correct options to be selected for full marks
     * - Partial credit scoring may be implemented
     * - More complex than multiple choice but good for comprehensive testing

     * Example: "Which of the following are programming languages?"
     * with options: [Java, HTML, CSS, Python, HTTP]
     * Correct answers: ["Java", "Python"]
     */
    MULTIPLE_SELECT,

    /**
     * Fill in the Blank question type.

     * Characteristics:
     * - Presents a statement with one or more blank spaces
     * - Student types in the missing word(s) or phrase(s)
     * - Can be case-sensitive or case-insensitive
     * - May accept multiple correct answers (synonyms or variations)
     * - Requires text input validation and matching algorithms
     * - Good for testing specific knowledge and terminology

     * Example: "The _____ is the largest planet in our solar system."
     * Correct answer: "Jupiter"
     */
    FILL_IN_THE_BLANK,

    /**
     * Essay question type.

     * Characteristics:
     * - Presents an open-ended question or prompt
     * - Student writes a free-form response of varying length
     * - Requires manual grading by instructor/grader
     * - No automated scoring possible
     * - Suitable for testing writing skills, critical thinking, and deep understanding
     * - May include word count limits or formatting requirements

     * Example: "Discuss the impact of artificial intelligence on modern society."
     */
    ESSAY,

    /**
     * True/False question type.

     * Characteristics:
     * - Question presents a statement
     * - Student must select either "True" or "False"
     * - Stored choices typically ["True", "False"]
     * - Auto-gradable
     */
    TRUE_FALSE,

    /**
     * Matching question type.

     * Characteristics:
     * - Two lists (e.g., Column A and Column B)
     * - Student must match items correctly
     * - Stored as JSON or custom format (e.g., {"A1":"B3","A2":"B1"})
     * - Can be auto-graded
     */
    MATCHING;


    /*
     * Potential additional question types that could be added:

     * TRUE_FALSE - Simple true/false questions
     * MATCHING - Matching items from two columns
     * SHORT_ANSWER - Brief written responses (shorter than essays)
     * NUMERICAL - Questions requiring numerical answers
     * CODE - Programming questions with code submission
     * FILE_UPLOAD - Questions requiring file attachments
     * HOTSPOT - Image-based questions with clickable areas
     * DRAG_AND_DROP - Interactive ordering or matching questions
     */

}