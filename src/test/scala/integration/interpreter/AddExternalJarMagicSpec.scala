package integration.interpreter

import java.io.OutputStream
import scala.tools.nsc.interpreter._
import java.net.{URL, URLClassLoader}

import com.ibm.spark.interpreter.ScalaInterpreter
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, FunSpec}

class AddExternalJarMagicSpec extends FunSpec with Matchers with MockitoSugar {
  describe("ScalaInterpreter") {
    describe("#addJars") {
      it("should be able to load an external jar") {
        val testJarUrl = this.getClass.getClassLoader.getResource("TestJar.jar")
        //val interpreter = new ScalaInterpreter(List(), Console.out)
        val interpreter = new ScalaInterpreter(List(), mock[OutputStream])
        interpreter.start()

        //
        // NOTE: This can be done with any jar. I have tested it previously by
        //       downloading jgoodies, placing it in /tmp/... and loading it.
        //

        // Should fail since jar was not added to paths
        interpreter.interpret(
          "import com.ibm.testjar.TestClass")._1 should be (IR.Error)

        // Add jar to paths
        interpreter.addJars(testJarUrl)

        // Should now succeed
        interpreter.interpret(
          "import com.ibm.testjar.TestClass")._1 should be (IR.Success)

        // Should now run
        interpreter.interpret(
          """println(new TestClass().sayHello("Chip"))"""
        ) should be (IR.Success, Left("Hello, Chip"))
      }

      it("should be able to add multiple jars at once") {
        val testJar1Url =
          this.getClass.getClassLoader.getResource("TestJar.jar")
        val testJar2Url =
          this.getClass.getClassLoader.getResource("TestJar2.jar")
        val interpreter = new ScalaInterpreter(List(), mock[OutputStream])
        interpreter.start()

        // Should fail since jars were not added to paths
        interpreter.interpret(
          "import com.ibm.testjar.TestClass")._1 should be (IR.Error)
        interpreter.interpret(
          "import com.ibm.testjar2.TestClass")._1 should be (IR.Error)

        // Add jars to paths
        interpreter.addJars(testJar1Url, testJar2Url)

        // Should now succeed
        interpreter.interpret(
          "import com.ibm.testjar.TestClass")._1 should be (IR.Success)
        interpreter.interpret(
          "import com.ibm.testjar2.TestClass")._1 should be (IR.Success)

        // Should now run
        interpreter.interpret(
          """println(new com.ibm.testjar.TestClass().sayHello("Chip"))"""
        ) should be (IR.Success, Left("Hello, Chip"))
        interpreter.interpret(
          """println(new com.ibm.testjar2.TestClass().CallMe())"""
        ) should be (IR.Success, Left("3"))
      }
    }
  }
}
