package org.kframework.kdoc

import org.junit._
import Assert._

class KDocTest {
  /*
  @Test def simple() {
    assertEquals(
      """
        |\begin{document}
        |
        |require "domains.k"
        | \begin{module}{
        |\moduleName{X}
        |  import INT
        |
        |  \begin{syntaxBlock}
        |    Foo ::= "x"
        |  \end{syntaxBlock}
        |  \begin{syntaxBlock}
        |    Foo ::= "y" [ \kattribute{latex}("specialLatexForY") ]
        |  \end{syntaxBlock}
        |  \krule{x => specialLatexForY}{x}{specialLatexForY}
        |  \kcontext{x}{specialLatexForY}{}
        |\end{module}
        |\end{document}
      """.stripMargin.trim
      , new KDoc("")(
        """
        | require "domains.k"
        | module X
        |   imports INT
        |   syntax Foo ::= "x"
        |   syntax Foo ::= "y" [latex("specialLatexForY")]
        |   rule x => y requires x ensures y
        |   rule <k> x => y </k> requires x ensures y
        |   context x requires y
        |
        |   configuration <k> x </k>
        | endmodule
        """.stripMargin).split("\n").map(_.replaceAll("\\s+$", "")).mkString("\n"))
  }
  */
}
