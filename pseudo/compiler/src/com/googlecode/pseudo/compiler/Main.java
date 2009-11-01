package com.googlecode.pseudo.compiler;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import code.googlecode.pseudo.compiler.model.Script;

import com.googlecode.pseudo.compiler.analysis.Enter;
import com.googlecode.pseudo.compiler.analysis.TypeCheck;
import com.googlecode.pseudo.compiler.ast.ASTGrammarEvaluator;
import com.googlecode.pseudo.compiler.ast.BooleanLiteralToken;
import com.googlecode.pseudo.compiler.ast.CharLiteralToken;
import com.googlecode.pseudo.compiler.ast.IdToken;
import com.googlecode.pseudo.compiler.ast.Node;
import com.googlecode.pseudo.compiler.ast.Start;
import com.googlecode.pseudo.compiler.ast.StringLiteralToken;
import com.googlecode.pseudo.compiler.ast.ValueLiteralToken;
import com.googlecode.pseudo.compiler.gen.Gen;
import com.googlecode.pseudo.compiler.tools.PseudoAnalyzers;
import com.googlecode.pseudo.compiler.tools.PseudoTerminalEvaluator;

import fr.umlv.tatoo.runtime.buffer.impl.LocationTracker;
import fr.umlv.tatoo.runtime.buffer.impl.ReaderWrapper;

public class Main {
  public static void main(String[] args) throws IOException {
    Reader reader;
    String scriptName;
    if (args.length == 1) {
      scriptName = args[0];
      reader = new FileReader(scriptName);
      int index = scriptName.lastIndexOf('.');
      if (index != -1)
        scriptName = scriptName.substring(0, index);
    } else {
      scriptName = "script";
      reader = new InputStreamReader(System.in);
    }
    
    final LocationMap locationMap = new LocationMap();
    final LocationTracker tracker = new LocationTracker();
    ReaderWrapper buffer = new ReaderWrapper(reader, tracker);
    
    PseudoTerminalEvaluator<CharSequence> terminalEvaluator = new PseudoTerminalEvaluator<CharSequence>() {
      private <N extends Node> N locate(N node) {
        locationMap.setLocation(node, tracker.getLineNumber(), tracker.getColumnNumber());
        return node;
      }
      
      @Override
      public void comment(CharSequence data) {
        // do nothing
      }
      
      @Override
      public BooleanLiteralToken boolean_literal(CharSequence data) {
        return locate(new BooleanLiteralToken(Boolean.parseBoolean(data.toString())));
      }

      @Override
      public CharLiteralToken char_literal(CharSequence data) {
        return locate(new CharLiteralToken(data.charAt(0)));
      }

      @Override
      public ValueLiteralToken value_literal(CharSequence data) {
        String text = data.toString();
        Object value;
        try {
          value = Integer.parseInt(text);
        } catch(NumberFormatException e) {
          value = Double.parseDouble(text);
        }
        return locate(new ValueLiteralToken(value));
      }

      @Override
      public StringLiteralToken string_literal(CharSequence data) {
        String text = data.toString();
        return locate(new StringLiteralToken(text.substring(1, text.length() - 1)));
      }
      
      @Override
      public IdToken id(CharSequence data) {
        return locate(new IdToken(data.toString()));
      }
    };
    
    ASTGrammarEvaluator grammarEvaluator = new ASTGrammarEvaluator() {
      @Override
      protected void computeAnnotation(Node node) {
        locationMap.setLocation(node, tracker.getLineNumber(), tracker.getColumnNumber());
      }
    };
    
    // lexer + parser
    PseudoAnalyzers.run(buffer, terminalEvaluator, grammarEvaluator, null, null);
    
    Start start = grammarEvaluator.getStart();
    //System.out.println(start);
    
    // compiler
    Script script = new Script(scriptName);
    CompilerErrorReporter errorReporter = new CompilerErrorReporter(locationMap);
    Enter enter = new Enter(script, errorReporter, locationMap);
    enter.enter(start);
    
    TypeCheck typeCheck = new TypeCheck(script, errorReporter);
    typeCheck.typeCheck();
    
    if (errorReporter.isOnError()) {
      return;
    }
    
    Gen genVisitor = new Gen(script, errorReporter, locationMap, typeCheck);
    genVisitor.gen(start);
  }
}
