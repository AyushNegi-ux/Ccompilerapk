package com.example

import com.example.compiler.CCompiler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CCompilerUnitTest {

    @Test
    fun testHelloWorld() {
        val code = """
            #include <stdio.h>
            int main() {
                printf("Hello, World!\n");
                return 0;
            }
        """.trimIndent()

        val result = CCompiler.compileAndRun(code)
        assertTrue(result.isSuccess)
        assertEquals("Hello, World!\n", result.output)
        assertEquals(0, result.exitCode)
    }

    @Test
    fun testArithmeticAndVariables() {
        val code = """
            #include <stdio.h>
            int main() {
                int a = 12;
                int b = 4;
                printf("Sum: %d, Prod: %d\n", a + b, a * b);
                return 0;
            }
        """.trimIndent()

        val result = CCompiler.compileAndRun(code)
        assertTrue(result.isSuccess)
        assertEquals("Sum: 16, Prod: 48\n", result.output)
    }

    @Test
    fun testLoopAndAccumulator() {
        val code = """
            #include <stdio.h>
            int main() {
                int sum = 0;
                for (int i = 1; i <= 5; i++) {
                    sum += i;
                }
                printf("Total: %d\n", sum);
                return 0;
            }
        """.trimIndent()

        val result = CCompiler.compileAndRun(code)
        assertTrue(result.isSuccess)
        assertEquals("Total: 15\n", result.output)
    }

    @Test
    fun testScanfInput() {
        val code = """
            #include <stdio.h>
            int main() {
                int a, b;
                scanf("%d %d", &a, &b);
                printf("Result: %d\n", a * b);
                return 0;
            }
        """.trimIndent()

        val result = CCompiler.compileAndRun(code, stdinInput = "6 7")
        assertTrue(result.isSuccess)
        assertEquals("Result: 42\n", result.output)
    }

    @Test
    fun testPointersSwap() {
        val code = """
            #include <stdio.h>
            void swap(int *a, int *b) {
                int temp = *a;
                *a = *b;
                *b = temp;
            }
            int main() {
                int x = 10;
                int y = 20;
                swap(&x, &y);
                printf("%d %d\n", x, y);
                return 0;
            }
        """.trimIndent()

        val result = CCompiler.compileAndRun(code)
        assertTrue(result.isSuccess)
        assertEquals("20 10\n", result.output)
    }

    @Test
    fun testRecursion() {
        val code = """
            #include <stdio.h>
            int factorial(int n) {
                if (n <= 1) return 1;
                return n * factorial(n - 1);
            }
            int main() {
                printf("Fact: %d\n", factorial(5));
                return 0;
            }
        """.trimIndent()

        val result = CCompiler.compileAndRun(code)
        assertTrue(result.isSuccess)
        assertEquals("Fact: 120\n", result.output)
    }

    @Test
    fun testArmstrongNumber() {
        val code = """
            #include <stdio.h>
            int main() {
                int n = 153;
                int original = n;
                int sum = 0;
                while (n > 0) {
                    int digit = n % 10;
                    sum += digit * digit * digit;
                    n /= 10;
                }
                if (sum == original) {
                    printf("Armstrong\n");
                } else {
                    printf("Not Armstrong\n");
                }
                return 0;
            }
        """.trimIndent()

        val result = CCompiler.compileAndRun(code)
        assertTrue(result.isSuccess)
        assertEquals("Armstrong\n", result.output)
    }

    @Test
    fun testReverseNumber() {
        val code = """
            #include <stdio.h>
            int main() {
                int n = 12345;
                int reversed = 0;
                while (n > 0) {
                    reversed = reversed * 10 + (n % 10);
                    n /= 10;
                }
                printf("Reversed: %d\n", reversed);
                return 0;
            }
        """.trimIndent()

        val result = CCompiler.compileAndRun(code)
        assertTrue(result.errorOutput, result.isSuccess)
        assertEquals("Reversed: 54321\n", result.output)
    }

    @Test
    fun testAllCurriculumChallengeSolutions() {
        val failed = mutableListOf<String>()
        for (ch in com.example.data.InitialCurriculum.challenges) {
            val result = CCompiler.compileAndRun(ch.solutionCode, ch.testInputs)
            if (!result.isSuccess) {
                failed.add("Challenge #${ch.id} '${ch.title}' FAILED to compile/run: ${result.errorOutput}")
            } else if (result.output.trim() != ch.expectedOutputs.trim()) {
                failed.add("Challenge #${ch.id} '${ch.title}' MISMATCH:\nExpected:\n${ch.expectedOutputs}\nGot:\n${result.output}")
            }
        }
        if (failed.isNotEmpty()) {
            throw AssertionError("Failed challenges:\n" + failed.joinToString("\n---\n"))
        }
    }

    @Test
    fun testAllDefaultProjects() {
        val failed = mutableListOf<String>()
        for (proj in com.example.data.InitialCurriculum.defaultProjects) {
            val result = CCompiler.compileAndRun(proj.code, "")
            if (!result.isSuccess) {
                failed.add("Project #${proj.id} '${proj.title}' FAILED to compile/run: ${result.errorOutput}")
            }
        }
        if (failed.isNotEmpty()) {
            throw AssertionError("Failed default projects:\n" + failed.joinToString("\n---\n"))
        }
    }
}
