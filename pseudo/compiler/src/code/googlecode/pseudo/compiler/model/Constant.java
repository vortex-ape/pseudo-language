package code.googlecode.pseudo.compiler.model;

import code.googlecode.pseudo.compiler.model.Vars.AbstractVar;
import code.googlecode.pseudo.compiler.model.Vars.MemberVar;

import com.googlecode.pseudo.compiler.Type;
import com.googlecode.pseudo.compiler.ast.ConstDef;

public class Constant extends AbstractVar implements MemberVar {
  private final ConstDef constDef;
  private Type type;
  
  public Constant(String name, ConstDef constDef) {
    super(name);
    this.constDef = constDef;
  }
  
  public ConstDef getConstDef() {
    return constDef;
  }
  
  @Override
  public Type getType() {
    assert type != null;
    return type;
  }
  public void setType(Type type) {
    this.type = type;
  }
  
  @Override
  public boolean isReadOnly() {
    return true;
  }
}
