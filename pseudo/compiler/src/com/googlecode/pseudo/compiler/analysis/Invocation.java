package com.googlecode.pseudo.compiler.analysis;

import code.googlecode.pseudo.compiler.model.Functions.NamedFunction;

import com.googlecode.pseudo.compiler.Types.FunType;

public class Invocation {
  private final FunType funType;
  private final NamedFunction function;
  
  public Invocation(FunType funType, /*maybenull*/NamedFunction function) {
    this.funType = funType;
    this.function = function;
  }
  
  public FunType getFunType() {
    return funType;
  }
  public /*maybenull*/NamedFunction getFunction() {
    return function;
  }
}
