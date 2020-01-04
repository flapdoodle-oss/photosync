package hello.tests;

import hello.JavaHello;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloTest {

  @Test
  public void testAssert() {
    assertThat(JavaHello.getHelloStringFromKotlin()).isEqualTo("Hello from Kotlin!");
    assertThat(hello.KotlinHelloKt.getHelloStringFromJava()).isEqualTo("Hello from Java!");

    System.out.println(hello.KotlinHelloKt.getHelloStringFromJava());
  }
}
