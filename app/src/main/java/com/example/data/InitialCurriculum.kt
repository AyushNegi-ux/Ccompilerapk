package com.example.data

import com.example.data.model.ChallengeEntity
import com.example.data.model.ProjectEntity

object InitialCurriculum {
    val defaultProjects = listOf(
        ProjectEntity(
            id = 1,
            title = "main.c",
            code = """#include <stdio.h>

// Developed by AYUSH NEGI

int main() {
    printf("======================================\n");
    printf("       Developed by AYUSH NEGI        \n");
    printf("======================================\n\n");

    int a = 20;
    int b = 30;
    int sum = a + b;

    printf("Addition Demo: %d + %d = %d\n", a, b, sum);
    printf("Select 'Practice' tab to try BCA chapter challenges!\n");
    return 0;
}
""",
            isTemplate = false
        ),

        // Chapter 1: Basics & Operators
        ProjectEntity(
            id = 2,
            title = "ch1_swap_numbers.c",
            code = """#include <stdio.h>

// BCA Chapter 1: Swap Two Numbers with & without 3rd variable

int main() {
    int a = 10, b = 20;
    printf("Original: a = %d, b = %d\n", a, b);

    // Swap using 3rd variable
    int temp = a;
    a = b;
    b = temp;
    printf("After Swap with temp: a = %d, b = %d\n", a, b);

    // Swap without 3rd variable using arithmetic
    a = a + b; // a becomes 30
    b = a - b; // b becomes 10
    a = a - b; // a becomes 20
    printf("After Swap without temp: a = %d, b = %d\n", a, b);

    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 3,
            title = "ch1_simple_interest.c",
            code = """#include <stdio.h>

// BCA Chapter 1: Calculate Simple Interest
// Formula: SI = (P * R * T) / 100

int main() {
    int principal = 5000;
    int rate = 5;       // 5 percent per year
    int time = 3;       // 3 years

    int simple_interest = (principal * rate * time) / 100;
    int total_amount = principal + simple_interest;

    printf("Principal Amount: %d\n", principal);
    printf("Rate of Interest: %d%%\n", rate);
    printf("Time Period:      %d years\n", time);
    printf("Simple Interest:  %d\n", simple_interest);
    printf("Total Amount:     %d\n", total_amount);

    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 4,
            title = "ch1_temperature_converter.c",
            code = """#include <stdio.h>

// BCA Chapter 1: Celsius to Fahrenheit Converter
// Formula: F = (C * 9/5) + 32

int main() {
    int celsius = 37;
    int fahrenheit = (celsius * 9 / 5) + 32;

    printf("Temperature in Celsius:    %d C\n", celsius);
    printf("Temperature in Fahrenheit: %d F\n", fahrenheit);

    return 0;
}
""",
            isTemplate = true
        ),

        // Chapter 2: Control Flow & Decisions
        ProjectEntity(
            id = 5,
            title = "ch2_largest_of_three.c",
            code = """#include <stdio.h>

// BCA Chapter 2: Find Largest of Three Numbers using nested if-else

int main() {
    int a = 45, b = 82, c = 37;
    int largest;

    if (a >= b && a >= c) {
        largest = a;
    } else if (b >= a && b >= c) {
        largest = b;
    } else {
        largest = c;
    }

    printf("Numbers: %d, %d, %d\n", a, b, c);
    printf("The largest number is: %d\n", largest);

    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 6,
            title = "ch2_calculator_switch.c",
            code = """#include <stdio.h>

// BCA Chapter 2: Menu Driven Calculator using switch-case

int main() {
    int a = 24, b = 6;
    int choice = 1; // 1: Add, 2: Subtract, 3: Multiply, 4: Divide

    printf("Operand A: %d, Operand B: %d\n", a, b);

    switch (choice) {
        case 1:
            printf("Addition: %d + %d = %d\n", a, b, a + b);
            break;
        case 2:
            printf("Subtraction: %d - %d = %d\n", a, b, a - b);
            break;
        case 3:
            printf("Multiplication: %d * %d = %d\n", a, b, a * b);
            break;
        case 4:
            if (b != 0) {
                printf("Division: %d / %d = %d\n", a, b, a / b);
            } else {
                printf("Error: Division by zero!\n");
            }
            break;
        default:
            printf("Invalid operation choice.\n");
    }

    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 7,
            title = "ch2_student_grades.c",
            code = """#include <stdio.h>

// BCA Chapter 2: Evaluate Student Grade based on Percentage

int main() {
    int marks1 = 85, marks2 = 78, marks3 = 92;
    int total = marks1 + marks2 + marks3;
    int percentage = total / 3;

    printf("Total Marks: %d / 300\n", total);
    printf("Percentage:  %d%%\n", percentage);

    if (percentage >= 90) {
        printf("Grade: A+ (Outstanding)\n");
    } else if (percentage >= 75) {
        printf("Grade: A (Distinction)\n");
    } else if (percentage >= 60) {
        printf("Grade: B (First Class)\n");
    } else if (percentage >= 40) {
        printf("Grade: C (Pass)\n");
    } else {
        printf("Grade: F (Fail)\n");
    }

    return 0;
}
""",
            isTemplate = true
        ),

        // Chapter 3: Loops & Iterations
        ProjectEntity(
            id = 8,
            title = "ch3_armstrong_number.c",
            code = """#include <stdio.h>

// BCA Chapter 3: Armstrong Number Checker
// An Armstrong number equals the sum of cubes of its digits (e.g., 153 = 1^3 + 5^3 + 3^3)

int main() {
    int num = 153;
    int temp = num;
    int sum = 0;

    while (temp > 0) {
        int rem = temp % 10;
        sum += (rem * rem * rem);
        temp /= 10;
    }

    printf("Checking number: %d\n", num);
    printf("Sum of cubes of digits: %d\n", sum);

    if (sum == num) {
        printf("Result: %d is an Armstrong Number!\n", num);
    } else {
        printf("Result: %d is NOT an Armstrong Number.\n", num);
    }

    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 9,
            title = "ch3_reverse_number.c",
            code = """#include <stdio.h>

// BCA Chapter 3: Reverse of a Number using % and /

int main() {
    int n = 12345;
    int original = n;
    int reversed = 0;

    while (n > 0) {
        int digit = n % 10;
        reversed = (reversed * 10) + digit;
        n /= 10;
    }

    printf("Original: %d\n", original);
    printf("Reversed: %d\n", reversed);

    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 10,
            title = "ch3_palindrome_number.c",
            code = """#include <stdio.h>

// BCA Chapter 3: Check if a number is Palindrome

int main() {
    int num = 1221;
    int temp = num;
    int rev = 0;

    while (temp > 0) {
        int rem = temp % 10;
        rev = (rev * 10) + rem;
        temp /= 10;
    }

    printf("Number:   %d\n", num);
    printf("Reversed: %d\n", rev);

    if (num == rev) {
        printf("Result: It is a Palindrome Number!\n");
    } else {
        printf("Result: It is NOT a Palindrome.\n");
    }

    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 11,
            title = "ch3_prime_checker.c",
            code = """#include <stdio.h>

// BCA Chapter 3: Check if a Number is Prime

int main() {
    int n = 29;
    int isPrime = 1;

    if (n <= 1) {
        isPrime = 0;
    } else {
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                isPrime = 0;
                break;
            }
        }
    }

    if (isPrime) {
        printf("%d is a Prime Number!\n", n);
    } else {
        printf("%d is NOT a Prime Number.\n", n);
    }

    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 12,
            title = "ch3_star_triangle.c",
            code = """#include <stdio.h>

// BCA Chapter 3: Print Star Pattern (Right Angled Triangle)

int main() {
    int rows = 5;

    printf("Star Triangle (%d rows):\n", rows);
    for (int i = 1; i <= rows; i++) {
        for (int j = 1; j <= i; j++) {
            printf("* ");
        }
        printf("\n");
    }

    return 0;
}
""",
            isTemplate = true
        ),

        // Chapter 4: Functions & Recursion
        ProjectEntity(
            id = 13,
            title = "ch4_factorial_recursion.c",
            code = """#include <stdio.h>

// BCA Chapter 4: Factorial using Recursion

int factorial(int n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);
}

int main() {
    int num = 6;
    int result = factorial(num);
    printf("Factorial of %d is: %d\n", num, result);
    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 14,
            title = "ch4_fibonacci_func.c",
            code = """#include <stdio.h>

// BCA Chapter 4: Fibonacci Sequence using Function

int fib(int n) {
    if (n <= 1) return n;
    return fib(n - 1) + fib(n - 2);
}

int main() {
    int count = 7;
    printf("Fibonacci Series (first %d terms):\n", count);
    for (int i = 0; i < count; i++) {
        printf("%d ", fib(i));
    }
    printf("\n");
    return 0;
}
""",
            isTemplate = true
        ),

        // Chapter 5: Arrays
        ProjectEntity(
            id = 15,
            title = "ch5_bubble_sort.c",
            code = """#include <stdio.h>

// BCA Chapter 5: Bubble Sort Algorithm on Array

int main() {
    int arr[5] = {64, 25, 12, 22, 11};
    int n = 5;

    printf("Original: ");
    for (int i = 0; i < n; i++) printf("%d ", arr[i]);
    printf("\n");

    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }

    printf("Sorted:   ");
    for (int i = 0; i < n; i++) printf("%d ", arr[i]);
    printf("\n");

    return 0;
}
""",
            isTemplate = true
        ),
        ProjectEntity(
            id = 16,
            title = "ch5_linear_search.c",
            code = """#include <stdio.h>

// BCA Chapter 5: Linear Search in Array

int main() {
    int arr[6] = {15, 32, 47, 88, 93, 21};
    int n = 6;
    int target = 88;
    int foundIndex = -1;

    printf("Array elements: ");
    for (int i = 0; i < n; i++) {
        printf("%d ", arr[i]);
    }
    printf("\nTarget to find: %d\n", target);

    for (int i = 0; i < n; i++) {
        if (arr[i] == target) {
            foundIndex = i;
            break;
        }
    }

    if (foundIndex != -1) {
        printf("Element %d found at index %d\n", target, foundIndex);
    } else {
        printf("Element %d not found\n", target);
    }

    return 0;
}
""",
            isTemplate = true
        ),

        // Chapter 6: Strings
        ProjectEntity(
            id = 17,
            title = "ch6_string_operations.c",
            code = """#include <stdio.h>

// BCA Chapter 6: Calculate String Length without library function

int main() {
    char str[50] = "BCA Computer Science";
    int length = 0;

    while (str[length] != '\0') {
        length++;
    }

    printf("String: \"%s\"\n", str);
    printf("Length: %d characters\n", length);

    return 0;
}
""",
            isTemplate = true
        ),

        // Chapter 7: Pointers
        ProjectEntity(
            id = 18,
            title = "ch7_pointer_basics.c",
            code = """#include <stdio.h>

// BCA Chapter 7: Pointer Basics & Swapping via Call by Reference

void swap(int *x, int *y) {
    int temp = *x;
    *x = *y;
    *y = temp;
}

int main() {
    int a = 100, b = 200;
    printf("Before swap: a = %d, b = %d\n", a, b);

    swap(&a, &b);
    printf("After swap:  a = %d, b = %d\n", a, b);

    return 0;
}
""",
            isTemplate = true
        ),

        // Chapter 8: Structures
        ProjectEntity(
            id = 19,
            title = "ch8_student_struct.c",
            code = """#include <stdio.h>

// BCA Chapter 8: Student Records using struct

struct Student {
    int rollNo;
    int marks;
};

int main() {
    struct Student s1;
    s1.rollNo = 101;
    s1.marks = 95;

    printf("--- Student Details ---\n");
    printf("Roll Number: %d\n", s1.rollNo);
    printf("Marks:       %d / 100\n", s1.marks);

    if (s1.marks >= 40) {
        printf("Status:      PASSED\n");
    } else {
        printf("Status:      FAILED\n");
    }

    return 0;
}
""",
            isTemplate = true
        )
    )

    // =========================================================
    // BCA CHAPTER PRACTICE CHALLENGES
    // Note: starterCode contains ONLY the instructions, hint &
    // starter skeleton. It does NOT contain the full solution,
    // so students do it themselves to learn!
    // solutionCode contains the complete reference solution.
    // =========================================================
    val challenges = listOf(
        // Chapter 1: Basics & Operators
        ChallengeEntity(
            id = 1,
            title = "Hello, C!",
            category = "Ch 1: Basics",
            difficulty = "Easy",
            description = "Write a C program that prints the exact phrase:\nHello, World!\n\nInclude <stdio.h> and terminate the output with a newline.",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 1: Hello World
// Task: Print "Hello, World!" followed by a newline.
// Do it yourself to learn!

int main() {
    // TODO: Use printf to output the greeting

    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    printf("Hello, World!\n");
    return 0;
}
""",
            testInputs = "",
            expectedOutputs = "Hello, World!\n",
            hints = "Use printf(\"Hello, World!\\n\"); inside your main() function.",
            xpReward = 20
        ),
        ChallengeEntity(
            id = 2,
            title = "Rectangle Area & Perimeter",
            category = "Ch 1: Basics",
            difficulty = "Easy",
            description = "Prompt with printf and read length and width using scanf. Calculate and print:\nArea: <area>\nPerimeter: <perimeter>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 1: Rectangle Area & Perimeter
// Task: Prompt with printf, read length and width, then compute and print:
//   Area: <area>
//   Perimeter: <perimeter>

int main() {
    int length, width;
    // Step 1: Prompt and read input using printf and scanf
    printf("Enter length and width: ");
    scanf("%d %d", &length, &width);
    
    // Step 2: Calculate Area = length * width
    // Step 3: Calculate Perimeter = 2 * (length + width)
    
    // Step 4: Print results
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int length, width;
    printf("Enter length and width: ");
    scanf("%d %d", &length, &width);
    int area = length * width;
    int perimeter = 2 * (length + width);
    printf("Area: %d\nPerimeter: %d\n", area, perimeter);
    return 0;
}
""",
            testInputs = "5 3",
            expectedOutputs = "Enter length and width: Area: 15\nPerimeter: 16\n",
            hints = "Use printf to prompt first, then scanf(\"%d %d\", &length, &width). Area is length * width and perimeter is 2 * (length + width).",
            xpReward = 25
        ),
        ChallengeEntity(
            id = 3,
            title = "Swap Two Numbers",
            category = "Ch 1: Basics",
            difficulty = "Easy",
            description = "Prompt with printf and read two integers A and B. Swap their values and print:\nBefore: A=<A> B=<B>\nAfter: A=<B> B=<A>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 1: Swap Two Numbers
// Task: Prompt with printf, read a and b, print Before values, swap them using a temporary variable, and print After values.

int main() {
    int a, b;
    // Step 1: Prompt and read a and b
    printf("Enter two numbers: ");
    scanf("%d %d", &a, &b);
    
    // Step 2: Print Before: A=<val> B=<val>
    
    // Step 3: Swap using temp variable:
    // int temp = a; a = b; b = temp;
    
    // Step 4: Print After: A=<val> B=<val>
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int a, b;
    printf("Enter two numbers: ");
    scanf("%d %d", &a, &b);
    printf("Before: A=%d B=%d\n", a, b);
    int temp = a;
    a = b;
    b = temp;
    printf("After: A=%d B=%d\n", a, b);
    return 0;
}
""",
            testInputs = "10 20",
            expectedOutputs = "Enter two numbers: Before: A=10 B=20\nAfter: A=20 B=10\n",
            hints = "Save 'a' in a temporary variable before assigning 'b' to 'a'.",
            xpReward = 25
        ),
        ChallengeEntity(
            id = 4,
            title = "Simple Interest Calculator",
            category = "Ch 1: Basics",
            difficulty = "Easy",
            description = "Prompt with printf and read Principal (P), Rate (R), and Time in years (T). Compute simple interest:\nSI = (P * R * T) / 100\nOutput:\nSI: <val>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 1: Simple Interest
// Formula: SI = (P * R * T) / 100

int main() {
    int p, r, t;
    // Step 1: Prompt and read p, r, t
    printf("Enter principal, rate, time: ");
    scanf("%d %d %d", &p, &r, &t);
    
    // Step 2: Calculate SI
    
    // Step 3: Print "SI: <val>\n"
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int p, r, t;
    printf("Enter principal, rate, time: ");
    scanf("%d %d %d", &p, &r, &t);
    int si = (p * r * t) / 100;
    printf("SI: %d\n", si);
    return 0;
}
""",
            testInputs = "1000 5 2",
            expectedOutputs = "Enter principal, rate, time: SI: 100\n",
            hints = "Multiply p * r * t, then divide the result by 100.",
            xpReward = 25
        ),

        // Chapter 2: Conditions
        ChallengeEntity(
            id = 5,
            title = "Even or Odd Checker",
            category = "Ch 2: Conditions",
            difficulty = "Easy",
            description = "Prompt with printf and read an integer. If it is even, print \"Even\". If it is odd, print \"Odd\".",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 2: Even or Odd Checker
// Task: Read an integer and print "Even" or "Odd".

int main() {
    int num;
    // Step 1: Prompt and read num
    printf("Enter a number: ");
    scanf("%d", &num);
    
    // Step 2: Check condition using modulo operator (%)
    // if (num % 2 == 0) ...
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int num;
    printf("Enter a number: ");
    scanf("%d", &num);
    if (num % 2 == 0) {
        printf("Even\n");
    } else {
        printf("Odd\n");
    }
    return 0;
}
""",
            testInputs = "8",
            expectedOutputs = "Enter a number: Even\n",
            hints = "Use the modulo operator %: if (num % 2 == 0) the number is even.",
            xpReward = 25
        ),
        ChallengeEntity(
            id = 6,
            title = "Largest of Three Numbers",
            category = "Ch 2: Conditions",
            difficulty = "Easy",
            description = "Prompt with printf and read three integers. Find the largest among them and print:\nLargest: <value>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 2: Largest of Three Numbers
// Task: Prompt with printf, read a, b, c and print "Largest: <value>"

int main() {
    int a, b, c;
    // Step 1: Prompt and read a, b, c
    printf("Enter three numbers: ");
    scanf("%d %d %d", &a, &b, &c);
    
    // Step 2: Use if-else conditions to find the largest
    
    // Step 3: Print result
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int a, b, c;
    printf("Enter three numbers: ");
    scanf("%d %d %d", &a, &b, &c);
    int max = a;
    if (b > max) max = b;
    if (c > max) max = c;
    printf("Largest: %d\n", max);
    return 0;
}
""",
            testInputs = "15 42 27",
            expectedOutputs = "Enter three numbers: Largest: 42\n",
            hints = "Initialize max = a; if (b > max) max = b; if (c > max) max = c;",
            xpReward = 30
        ),
        ChallengeEntity(
            id = 7,
            title = "Leap Year Checker",
            category = "Ch 2: Conditions",
            difficulty = "Easy",
            description = "Prompt with printf and read a year. If it is a leap year, print \"Leap Year\". Otherwise print \"Not Leap Year\".\nA year is a leap year if it is divisible by 400, or divisible by 4 and not 100.",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 2: Leap Year Checker
// Rule: Divisible by 400 OR (divisible by 4 AND NOT divisible by 100)

int main() {
    int year;
    // Step 1: Prompt and read year
    printf("Enter a year: ");
    scanf("%d", &year);
    
    // Step 2: Check leap year condition
    
    // Step 3: Print "Leap Year" or "Not Leap Year"
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int year;
    printf("Enter a year: ");
    scanf("%d", &year);
    if ((year % 400 == 0) || (year % 4 == 0 && year % 100 != 0)) {
        printf("Leap Year\n");
    } else {
        printf("Not Leap Year\n");
    }
    return 0;
}
""",
            testInputs = "2024",
            expectedOutputs = "Enter a year: Leap Year\n",
            hints = "Use: if ((year % 400 == 0) || (year % 4 == 0 && year % 100 != 0))",
            xpReward = 30
        ),
        ChallengeEntity(
            id = 8,
            title = "Simple Calculator (Switch)",
            category = "Ch 2: Conditions",
            difficulty = "Medium",
            description = "Prompt with printf and read two integers A and B, followed by an integer choice (1: Add, 2: Subtract, 3: Multiply, 4: Divide). Use a switch statement and output:\nResult: <value>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 2: Calculator using switch-case
// Task: Read a, b, choice and output "Result: <value>"

int main() {
    int a, b, choice;
    // Step 1: Prompt and read a, b, choice
    printf("Enter two numbers and choice (1-Add, 2-Sub, 3-Mul, 4-Div): ");
    scanf("%d %d %d", &a, &b, &choice);
    
    // Step 2: Use switch(choice) { case 1: ... case 2: ... }
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int a, b, choice;
    printf("Enter two numbers and choice (1-Add, 2-Sub, 3-Mul, 4-Div): ");
    scanf("%d %d %d", &a, &b, &choice);
    int res = 0;
    switch (choice) {
        case 1: res = a + b; break;
        case 2: res = a - b; break;
        case 3: res = a * b; break;
        case 4: res = (b != 0) ? (a / b) : 0; break;
        default: res = 0;
    }
    printf("Result: %d\n", res);
    return 0;
}
""",
            testInputs = "20 5 3",
            expectedOutputs = "Enter two numbers and choice (1-Add, 2-Sub, 3-Mul, 4-Div): Result: 100\n",
            hints = "Use switch (choice) with cases 1 to 4 and break after each case.",
            xpReward = 35
        ),

        // Chapter 3: Loops & Iterations
        ChallengeEntity(
            id = 9,
            title = "Reverse of a Number",
            category = "Ch 3: Loops",
            difficulty = "Easy",
            description = "Prompt with printf and read an integer N. Reverse its digits using a loop and arithmetic operators (% and /) and print:\nReversed: <value>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 3: Reverse of a Number
// Task: Read an integer N, reverse its digits using a while loop,
// and output "Reversed: <value>"

int main() {
    int n;
    // Step 1: Prompt and read n using printf and scanf
    printf("Enter a number: ");
    scanf("%d", &n);
    
    // Step 2: Initialize int reversed = 0;
    
    // Step 3: Loop while n > 0:
    //   - extract last digit: digit = n % 10
    //   - append to reversed: reversed = (reversed * 10) + digit
    //   - remove last digit:  n = n / 10
    
    // Step 4: Print "Reversed: %d\n", reversed
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int n;
    printf("Enter a number: ");
    scanf("%d", &n);
    int reversed = 0;
    while (n > 0) {
        reversed = (reversed * 10) + (n % 10);
        n /= 10;
    }
    printf("Reversed: %d\n", reversed);
    return 0;
}
""",
            testInputs = "12345",
            expectedOutputs = "Enter a number: Reversed: 54321\n",
            hints = "Inside while(n > 0), use: reversed = (reversed * 10) + (n % 10); then n /= 10;",
            xpReward = 35
        ),
        ChallengeEntity(
            id = 10,
            title = "Sum of Digits",
            category = "Ch 3: Loops",
            difficulty = "Easy",
            description = "Prompt with printf and read an integer N. Calculate the sum of its individual digits and print:\nSum: <value>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 3: Sum of Digits
// Task: Read an integer N, sum its digits, and output "Sum: <value>"

int main() {
    int n;
    // Step 1: Prompt and read n
    printf("Enter a number: ");
    scanf("%d", &n);
    
    // Step 2: Loop while n > 0 and add each (n % 10) to sum
    
    // Step 3: Print result
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int n;
    printf("Enter a number: ");
    scanf("%d", &n);
    int sum = 0;
    while (n > 0) {
        sum += n % 10;
        n /= 10;
    }
    printf("Sum: %d\n", sum);
    return 0;
}
""",
            testInputs = "5432",
            expectedOutputs = "Enter a number: Sum: 14\n",
            hints = "In a while(n > 0) loop, add n % 10 to sum and divide n by 10.",
            xpReward = 30
        ),
        ChallengeEntity(
            id = 11,
            title = "Armstrong Number",
            category = "Ch 3: Loops",
            difficulty = "Medium",
            description = "Prompt with printf and read an integer N (e.g. 153). An Armstrong number is equal to the sum of cubes of its digits (153 = 1^3 + 5^3 + 3^3 = 1 + 125 + 27 = 153).\nIf N is an Armstrong number, print \"Armstrong\". Otherwise print \"Not Armstrong\".",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 3: Armstrong Number
// Rule: A number is Armstrong if sum of cubes of digits == original number.
// Example: 153 -> (1*1*1) + (5*5*5) + (3*3*3) = 1 + 125 + 27 = 153

int main() {
    int n;
    // Step 1: Prompt and read n
    printf("Enter a number: ");
    scanf("%d", &n);
    
    // Step 2: Keep a copy: int original = n;
    // Step 3: Loop while n > 0:
    //         int rem = n % 10;
    //         sum += rem * rem * rem;
    //         n /= 10;
    // Step 4: Compare sum with original
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int n;
    printf("Enter a number: ");
    scanf("%d", &n);
    int original = n;
    int sum = 0;
    while (n > 0) {
        int rem = n % 10;
        sum += rem * rem * rem;
        n /= 10;
    }
    if (sum == original) {
        printf("Armstrong\n");
    } else {
        printf("Not Armstrong\n");
    }
    return 0;
}
""",
            testInputs = "153",
            expectedOutputs = "Enter a number: Armstrong\n",
            hints = "Store original = n before the loop. Sum each rem * rem * rem, then check if sum == original.",
            xpReward = 40
        ),
        ChallengeEntity(
            id = 12,
            title = "Palindrome Number",
            category = "Ch 3: Loops",
            difficulty = "Medium",
            description = "Prompt with printf and read an integer N. Check if it reads identically forwards and backwards.\nIf it is a palindrome, print \"Palindrome\". Otherwise print \"Not Palindrome\".",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 3: Palindrome Number
// Task: Read an integer N. Reverse it and check if original == reversed.

int main() {
    int n;
    // Step 1: Prompt and read n, and store original = n
    printf("Enter a number: ");
    scanf("%d", &n);
    
    // Step 2: Reverse the number using while loop
    
    // Step 3: If original == reversed, print "Palindrome\n", else "Not Palindrome\n"
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int n;
    printf("Enter a number: ");
    scanf("%d", &n);
    int original = n;
    int rev = 0;
    while (n > 0) {
        rev = (rev * 10) + (n % 10);
        n /= 10;
    }
    if (original == rev) {
        printf("Palindrome\n");
    } else {
        printf("Not Palindrome\n");
    }
    return 0;
}
""",
            testInputs = "1221",
            expectedOutputs = "Enter a number: Palindrome\n",
            hints = "Reverse the number into a variable 'rev'. If original == rev, print Palindrome.",
            xpReward = 40
        ),
        ChallengeEntity(
            id = 13,
            title = "Prime Number Checker",
            category = "Ch 3: Loops",
            difficulty = "Medium",
            description = "Prompt with printf and read an integer N. If N is prime, print \"Prime\". Otherwise print \"Not Prime\".",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 3: Prime Number Checker
// Task: Test if N has any divisors other than 1 and itself.

int main() {
    int n;
    // Step 1: Prompt and read n
    printf("Enter a number: ");
    scanf("%d", &n);
    
    // Step 2: Check if n <= 1 (not prime)
    
    // Step 3: For i from 2 up to i * i <= n:
    //         if n % i == 0, mark as not prime
    
    // Step 4: Print "Prime\n" or "Not Prime\n"
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int n;
    printf("Enter a number: ");
    scanf("%d", &n);
    int isPrime = 1;
    if (n <= 1) {
        isPrime = 0;
    } else {
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                isPrime = 0;
                break;
            }
        }
    }
    if (isPrime) {
        printf("Prime\n");
    } else {
        printf("Not Prime\n");
    }
    return 0;
}
""",
            testInputs = "29",
            expectedOutputs = "Enter a number: Prime\n",
            hints = "Use a boolean flag isPrime = 1. If any i divides n with remainder 0, set isPrime = 0 and break.",
            xpReward = 40
        ),
        ChallengeEntity(
            id = 14,
            title = "Sum of First and Last Digit",
            category = "Ch 3: Loops",
            difficulty = "Easy",
            description = "Prompt with printf and read an integer N (>= 10). Find its first digit and its last digit, then output:\nFirst: <first>\nLast: <last>\nSum: <sum>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 3: First and Last Digit
// Task: Extract last digit with n % 10.
// Extract first digit by dividing while first >= 10.

int main() {
    int n;
    // Step 1: Prompt and read n
    printf("Enter a number: ");
    scanf("%d", &n);
    
    // Step 2: int last = n % 10;
    // Step 3: int first = n; while (first >= 10) first /= 10;
    // Step 4: Print First, Last, and Sum
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int n;
    printf("Enter a number: ");
    scanf("%d", &n);
    int last = n % 10;
    int first = n;
    while (first >= 10) {
        first /= 10;
    }
    int sum = first + last;
    printf("First: %d\nLast: %d\nSum: %d\n", first, last, sum);
    return 0;
}
""",
            testInputs = "4729",
            expectedOutputs = "Enter a number: First: 4\nLast: 9\nSum: 13\n",
            hints = "Last digit is n % 10. For first digit, divide by 10 repeatedly until first < 10.",
            xpReward = 35
        ),
        ChallengeEntity(
            id = 15,
            title = "Greatest Common Divisor (GCD)",
            category = "Ch 3: Loops",
            difficulty = "Medium",
            description = "Prompt with printf and read two positive integers A and B. Find their Greatest Common Divisor (GCD/HCF) and print:\nGCD: <value>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 3: Greatest Common Divisor (GCD)
// Task: Compute GCD of a and b.

int main() {
    int a, b;
    // Step 1: Prompt and read a and b
    printf("Enter two numbers: ");
    scanf("%d %d", &a, &b);
    
    // Step 2: Use Euclidean loop:
    // while (b != 0) { int t = b; b = a % b; a = t; }
    
    // Step 3: Print "GCD: %d\n", a
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int a, b;
    printf("Enter two numbers: ");
    scanf("%d %d", &a, &b);
    while (b != 0) {
        int temp = b;
        b = a % b;
        a = temp;
    }
    printf("GCD: %d\n", a);
    return 0;
}
""",
            testInputs = "48 18",
            expectedOutputs = "Enter two numbers: GCD: 6\n",
            hints = "While b != 0: temp = b; b = a % b; a = temp. When b becomes 0, 'a' holds the GCD.",
            xpReward = 40
        ),
        ChallengeEntity(
            id = 16,
            title = "Multiplication Table",
            category = "Ch 3: Loops",
            difficulty = "Easy",
            description = "Prompt with printf and read an integer N. Print its multiplication table from 1 to 5 in the format:\n<N> x <multiplier> = <result>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 3: Multiplication Table (1 to 5)
// Task: Print lines: N x 1 = ..., up to N x 5 = ...

int main() {
    int n;
    // Step 1: Prompt and read n
    printf("Enter a number: ");
    scanf("%d", &n);
    
    // Step 2: Loop for i = 1 to 5 and print "N x i = (n * i)"
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int n;
    printf("Enter a number: ");
    scanf("%d", &n);
    for (int i = 1; i <= 5; i++) {
        printf("%d x %d = %d\n", n, i, n * i);
    }
    return 0;
}
""",
            testInputs = "7",
            expectedOutputs = "Enter a number: 7 x 1 = 7\n7 x 2 = 14\n7 x 3 = 21\n7 x 4 = 28\n7 x 5 = 35\n",
            hints = "Use a for loop from 1 to 5: printf(\"%d x %d = %d\\n\", n, i, n * i);",
            xpReward = 25
        ),

        // Chapter 4: Functions & Recursion
        ChallengeEntity(
            id = 17,
            title = "Factorial using Recursion",
            category = "Ch 4: Functions",
            difficulty = "Medium",
            description = "Write a recursive function int fact(int n) that returns the factorial of n. Prompt with printf, read n, and print:\nFactorial: <value>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 4: Factorial using Recursion
// Base case: if n <= 1 return 1
// Recursive step: return n * fact(n - 1)

int fact(int n) {
    // TODO: Write recursive logic here
}

int main() {
    int n;
    // Step 1: Prompt and read n
    printf("Enter a number: ");
    scanf("%d", &n);
    // Step 2: Call fact(n)
    // Step 3: Print "Factorial: %d\n", result
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int fact(int n) {
    if (n <= 1) return 1;
    return n * fact(n - 1);
}

int main() {
    int n;
    printf("Enter a number: ");
    scanf("%d", &n);
    printf("Factorial: %d\n", fact(n));
    return 0;
}
""",
            testInputs = "5",
            expectedOutputs = "Enter a number: Factorial: 120\n",
            hints = "In fact(n): if (n <= 1) return 1; else return n * fact(n - 1);",
            xpReward = 45
        ),
        ChallengeEntity(
            id = 18,
            title = "Power using Recursion",
            category = "Ch 4: Functions",
            difficulty = "Medium",
            description = "Write a recursive function int power(int base, int exp) that calculates base^exp. Prompt with printf, read base and exp, and print:\nResult: <value>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 4: Power using Recursion
// Base case: if exp == 0 return 1
// Recursive step: return base * power(base, exp - 1)

int power(int base, int exp) {
    // TODO: Write recursive power function
}

int main() {
    int b, e;
    // Step 1: Prompt and read b and e
    printf("Enter base and exponent: ");
    scanf("%d %d", &b, &e);
    // Step 2: Print "Result: %d\n", power(b, e)
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int power(int base, int exp) {
    if (exp == 0) return 1;
    return base * power(base, exp - 1);
}

int main() {
    int b, e;
    printf("Enter base and exponent: ");
    scanf("%d %d", &b, &e);
    printf("Result: %d\n", power(b, e));
    return 0;
}
""",
            testInputs = "2 4",
            expectedOutputs = "Enter base and exponent: Result: 16\n",
            hints = "Base case: exp == 0 returns 1. Recursive case: base * power(base, exp - 1).",
            xpReward = 45
        ),

        // Chapter 5: Arrays
        ChallengeEntity(
            id = 19,
            title = "Array Sum & Average",
            category = "Ch 5: Arrays",
            difficulty = "Easy",
            description = "An array of 5 integers is initialized: {10, 20, 30, 40, 50}. Calculate and print:\nSum: <sum>\nAverage: <avg>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 5: Array Sum and Average
// Task: Calculate sum and average of array elements.

int main() {
    int arr[5] = {10, 20, 30, 40, 50};
    int n = 5;
    
    // Step 1: Loop through array and accumulate sum
    
    // Step 2: Calculate average = sum / n
    
    // Step 3: Print Sum and Average
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int arr[5] = {10, 20, 30, 40, 50};
    int n = 5;
    int sum = 0;
    for (int i = 0; i < n; i++) {
        sum += arr[i];
    }
    int avg = sum / n;
    printf("Sum: %d\nAverage: %d\n", sum, avg);
    return 0;
}
""",
            testInputs = "",
            expectedOutputs = "Sum: 150\nAverage: 30\n",
            hints = "Iterate with for (int i = 0; i < 5; i++) sum += arr[i]; then avg = sum / 5.",
            xpReward = 30
        ),
        ChallengeEntity(
            id = 20,
            title = "Bubble Sort Ascending",
            category = "Ch 5: Arrays",
            difficulty = "Hard",
            description = "Sort the array {5, 1, 4, 2, 8} in ascending order using Bubble Sort algorithm. Print the sorted elements separated by spaces, ending with a newline.",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 5: Bubble Sort
// Task: Sort {5, 1, 4, 2, 8} using nested loops.

int main() {
    int arr[5] = {5, 1, 4, 2, 8};
    int n = 5;

    // Step 1: Implement bubble sort nested loops
    // for i from 0 to n-2:
    //   for j from 0 to n-i-2:
    //     if (arr[j] > arr[j+1]) swap
    
    // Step 2: Print sorted elements
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    int arr[5] = {5, 1, 4, 2, 8};
    int n = 5;
    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                int t = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = t;
            }
        }
    }
    for (int i = 0; i < n; i++) {
        printf("%d ", arr[i]);
    }
    printf("\n");
    return 0;
}
""",
            testInputs = "",
            expectedOutputs = "1 2 4 5 8 \n",
            hints = "Compare adjacent elements arr[j] and arr[j+1]. Swap if arr[j] > arr[j+1].",
            xpReward = 50
        ),

        // Chapter 6: Strings
        ChallengeEntity(
            id = 21,
            title = "String Length without strlen",
            category = "Ch 6: Strings",
            difficulty = "Easy",
            description = "Count the number of characters in the string \"BCA_Programming\" without using strlen. Output:\nLength: <val>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 6: String Length
// Task: Count characters until null terminator '\0' is encountered.

int main() {
    char str[50] = "BCA_Programming";
    int len = 0;

    // Step 1: While str[len] != '\0', increment len
    
    // Step 2: Print "Length: %d\n", len
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

int main() {
    char str[50] = "BCA_Programming";
    int len = 0;
    while (str[len] != '\0') {
        len++;
    }
    printf("Length: %d\n", len);
    return 0;
}
""",
            testInputs = "",
            expectedOutputs = "Length: 15\n",
            hints = "In C, strings terminate with '\\0'. Loop while str[len] != '\\0'.",
            xpReward = 30
        ),

        // Chapter 7: Pointers
        ChallengeEntity(
            id = 22,
            title = "Swap with Pointers (Call by Reference)",
            category = "Ch 7: Pointers",
            difficulty = "Medium",
            description = "Write a function void swap(int *x, int *y) that swaps values using pointer dereferencing (*). In main(), swap 50 and 99 and print:\nSwapped: A=<newA> B=<newB>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 7: Pointers & Call by Reference
// Task: Complete swap function using pointers.

void swap(int *x, int *y) {
    // TODO: Dereference pointers and swap values
}

int main() {
    int a = 50, b = 99;
    // Step 1: Call swap(&a, &b)
    
    // Step 2: Print "Swapped: A=%d B=%d\n", a, b
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

void swap(int *x, int *y) {
    int temp = *x;
    *x = *y;
    *y = temp;
}

int main() {
    int a = 50, b = 99;
    swap(&a, &b);
    printf("Swapped: A=%d B=%d\n", a, b);
    return 0;
}
""",
            testInputs = "",
            expectedOutputs = "Swapped: A=99 B=50\n",
            hints = "int temp = *x; *x = *y; *y = temp; and call with swap(&a, &b).",
            xpReward = 40
        ),

        // Chapter 8: Structures
        ChallengeEntity(
            id = 23,
            title = "Student Record Structure",
            category = "Ch 8: Structures",
            difficulty = "Medium",
            description = "Define a struct Student with rollNo (int) and marks (int). Create a student with rollNo=101 and marks=88. Print:\nRoll: <rollNo>\nMarks: <marks>",
            starterCode = """#include <stdio.h>

// BCA Practice Ch 8: Structures
// Task: Define struct Student, instantiate s, assign values, and print.

struct Student {
    // TODO: Define members rollNo and marks
};

int main() {
    // Step 1: struct Student s;
    // Step 2: s.rollNo = 101; s.marks = 88;
    // Step 3: Print Roll and Marks
    
    return 0;
}
""",
            solutionCode = """#include <stdio.h>

struct Student {
    int rollNo;
    int marks;
};

int main() {
    struct Student s;
    s.rollNo = 101;
    s.marks = 88;
    printf("Roll: %d\nMarks: %d\n", s.rollNo, s.marks);
    return 0;
}
""",
            testInputs = "",
            expectedOutputs = "Roll: 101\nMarks: 88\n",
            hints = "Define struct Student { int rollNo; int marks; }; and access fields using dot operator.",
            xpReward = 40
        )
    )
}
