/*
* generated by Xtext
*/
package org.eclipse.fx.ide.fxgraph.parser.antlr;

import java.io.InputStream;
import org.eclipse.xtext.parser.antlr.IAntlrTokenFileProvider;

public class FXGraphAntlrTokenFileProvider implements IAntlrTokenFileProvider {
	
	public InputStream getAntlrTokenFile() {
		ClassLoader classLoader = getClass().getClassLoader();
    	return classLoader.getResourceAsStream("at/bestsolution/efxclipse/tooling/fxgraph/parser/antlr/internal/InternalFXGraph.tokens");
	}
}