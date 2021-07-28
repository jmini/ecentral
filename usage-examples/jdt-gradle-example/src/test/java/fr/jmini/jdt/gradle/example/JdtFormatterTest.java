package fr.jmini.jdt.gradle.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class JdtFormatterTest {

    @Test
    void testFormatter() throws Exception {
        String javaCode = "public class MyClass{ "
                + "public static void main(String[] args) { "
                + "System.out.println(\"Hello World\");"
                + " }"
                + " }";

        String actual = JdtFormatter.format(javaCode);

        String expected = "public class MyClass {\n"
                + "\tpublic static void main(String[] args) {\n"
                + "\t\tSystem.out.println(\"Hello World\");\n"
                + "\t}\n"
                + "}";

        assertThat(actual).isEqualTo(expected);
    }
}