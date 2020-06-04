package butterknife;

import static butterknife.ButterKnife.trySet;

import java.lang.reflect.Field;

final class FieldUnbinder implements Unbinder {
  private final Object target;
  private final Field field;

  FieldUnbinder(Object target, Field field) {
    this.target = target;
    this.field = field;
  }

  @Override
  public void unbind() {
    trySet(field, target, null);
  }
}
