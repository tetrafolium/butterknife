package butterknife;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Arrays.asList;

import butterknife.compiler.ButterKnifeProcessor;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

public class UnbinderTest {
@Test
public void multipleBindings() {
	JavaFileObject source = JavaFileObjects.forSourceString(
		"test.Test",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import butterknife.BindView;\n"
		+ "import butterknife.OnClick;\n"
		+ "import butterknife.OnLongClick;\n"
		+ "public class Test {\n"
		+ "  @BindView(1) View view;\n"
		+ "  @BindView(2) View view2;\n"
		+ "  @OnClick(1) void doStuff() {}\n"
		+ "  @OnLongClick(1) boolean doMoreStuff() { return false; }\n"
		+ "}");

	JavaFileObject bindingSource = JavaFileObjects.forSourceString(
		"test/Test_ViewBinding",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.CallSuper;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import butterknife.Unbinder;\n"
		+ "import butterknife.internal.DebouncingOnClickListener;\n"
		+ "import butterknife.internal.Utils;\n"
		+ "import java.lang.IllegalStateException;\n"
		+ "import java.lang.Override;\n"
		+ "public class Test_ViewBinding implements Unbinder {\n"
		+ "  private Test target;\n"
		+ "  private View view1;\n"
		+ "  @UiThread\n"
		+ "  public Test_ViewBinding(final Test target, View source) {\n"
		+ "    this.target = target;\n"
		+ "    View view;\n"
		+
		"    view = Utils.findRequiredView(source, 1, \"field 'view', method 'doStuff', and method 'doMoreStuff'\");\n"
		+ "    target.view = view;\n"
		+ "    view1 = view;\n"
		+ "    view.setOnClickListener(new DebouncingOnClickListener() {\n"
		+ "      @Override\n"
		+ "      public void doClick(View p0) {\n"
		+ "        target.doStuff();\n"
		+ "      }\n"
		+ "    });\n"
		+
		"    view.setOnLongClickListener(new View.OnLongClickListener() {\n"
		+ "      @Override\n"
		+ "      public boolean onLongClick(View p0) {\n"
		+ "        return target.doMoreStuff();\n"
		+ "      }\n"
		+ "    });\n"
		+
		"    target.view2 = Utils.findRequiredView(source, 2, \"field 'view2'\");\n"
		+ "  }\n"
		+ "  @Override\n"
		+ "  @CallSuper\n"
		+ "  public void unbind() {\n"
		+ "    Test target = this.target;\n"
		+
		"    if (target == null) throw new IllegalStateException(\"Bindings already cleared.\");\n"
		+ "    this.target = null;\n"
		+ "    target.view = null;\n"
		+ "    target.view2 = null;\n"
		+ "    view1.setOnClickListener(null);\n"
		+ "    view1.setOnLongClickListener(null);\n"
		+ "    view1 = null;\n"
		+ "  }\n"
		+ "}");

	assertAbout(javaSource())
	.that(source)
	.withCompilerOptions("-Xlint:-processing")
	.processedWith(new ButterKnifeProcessor())
	.compilesWithoutWarnings()
	.and()
	.generatesSources(bindingSource);
}

@Test
public void unbindingThroughAbstractChild() {
	JavaFileObject source1 = JavaFileObjects.forSourceString(
		"test.Test", ""
		+ "package test;\n"
		+ "import butterknife.OnClick;\n"
		+ "public class Test {\n"
		+ "  @OnClick(1) void doStuff1() { }\n"
		+ "}");

	JavaFileObject source2 = JavaFileObjects.forSourceString(
		"test.TestOne", ""
		+ "package test;\n"
		+ "public abstract class TestOne extends Test {\n"
		+ "}");

	JavaFileObject source3 = JavaFileObjects.forSourceString(
		"test.TestTwo", ""
		+ "package test;\n"
		+ "import butterknife.OnClick;\n"
		+ "class TestTwo extends TestOne {\n"
		+ "  @OnClick(1) void doStuff2() { }\n"
		+ "}");

	JavaFileObject binding1Source = JavaFileObjects.forSourceString(
		"test/Test_ViewBinding",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.CallSuper;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import butterknife.Unbinder;\n"
		+ "import butterknife.internal.DebouncingOnClickListener;\n"
		+ "import butterknife.internal.Utils;\n"
		+ "import java.lang.IllegalStateException;\n"
		+ "import java.lang.Override;\n"
		+ "public class Test_ViewBinding implements Unbinder {\n"
		+ "  private Test target;\n"
		+ "  private View view1;\n"
		+ "  @UiThread\n"
		+ "  public Test_ViewBinding(final Test target, View source) {\n"
		+ "    this.target = target;\n"
		+ "    View view;\n"
		+
		"    view = Utils.findRequiredView(source, 1, \"method 'doStuff1'\");\n"
		+ "    view1 = view;\n"
		+ "    view.setOnClickListener(new DebouncingOnClickListener() {\n"
		+ "      @Override\n"
		+ "      public void doClick(View p0) {\n"
		+ "        target.doStuff1();\n"
		+ "      }\n"
		+ "    });\n"
		+ "  }\n"
		+ "  @Override\n"
		+ "  @CallSuper\n"
		+ "  public void unbind() {\n"
		+
		"    if (target == null) throw new IllegalStateException(\"Bindings already cleared.\");\n"
		+ "    target = null;\n"
		+ "    view1.setOnClickListener(null);\n"
		+ "    view1 = null;\n"
		+ "  }\n"
		+ "}");

	JavaFileObject binding2Source = JavaFileObjects.forSourceString(
		"test/TestTwo_ViewBinding",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import butterknife.internal.DebouncingOnClickListener;\n"
		+ "import butterknife.internal.Utils;\n"
		+ "import java.lang.IllegalStateException;\n"
		+ "import java.lang.Override;\n"
		+ "public class TestTwo_ViewBinding extends Test_ViewBinding {\n"
		+ "  private TestTwo target;\n"
		+ "  private View view1;\n"
		+ "  @UiThread\n"
		+
		"  public TestTwo_ViewBinding(final TestTwo target, View source) {\n"
		+ "    super(target, source);\n"
		+ "    this.target = target;\n"
		+ "    View view;\n"
		+
		"    view = Utils.findRequiredView(source, 1, \"method 'doStuff2'\");\n"
		+ "    view1 = view;\n"
		+ "    view.setOnClickListener(new DebouncingOnClickListener() {\n"
		+ "      @Override\n"
		+ "      public void doClick(View p0) {\n"
		+ "        target.doStuff2();\n"
		+ "      }\n"
		+ "    });\n"
		+ "  }\n"
		+ "  @Override\n"
		+ "  public void unbind() {\n"
		+
		"    if (target == null) throw new IllegalStateException(\"Bindings already cleared.\");\n"
		+ "    target = null;\n"
		+ "    view1.setOnClickListener(null);\n"
		+ "    view1 = null;\n"
		+ "    super.unbind();\n"
		+ "  }\n"
		+ "}");

	assertAbout(javaSources())
	.that(asList(source1, source2, source3))
	.withCompilerOptions("-Xlint:-processing")
	.processedWith(new ButterKnifeProcessor())
	.compilesWithoutWarnings()
	.and()
	.generatesSources(binding1Source, binding2Source);
}

@Test
public void fullIntegration() {
	JavaFileObject sourceA = JavaFileObjects.forSourceString(
		"test.A",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.ColorInt;\n"
		+ "import butterknife.BindColor;\n"
		+ "public class A {\n"
		+ "  @BindColor(android.R.color.black) @ColorInt int blackColor;\n"
		+ "  public A(View view) {\n"
		+ "  }\n"
		+ "}\n");

	JavaFileObject sourceB = JavaFileObjects.forSourceString(
		"test.B",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.ColorInt;\n"
		+ "import butterknife.BindColor;\n"
		+ "public class B extends A {\n"
		+ "  @BindColor(android.R.color.white) @ColorInt int whiteColor;\n"
		+ "  public B(View view) {\n"
		+ "    super(view);\n"
		+ "  }\n"
		+ "}\n");

	JavaFileObject sourceC = JavaFileObjects.forSourceString(
		"test.C",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.ColorInt;\n"
		+ "import butterknife.BindView;\n"
		+ "import butterknife.BindColor;\n"
		+ "public class C extends B {\n"
		+
		"  @BindColor(android.R.color.transparent) @ColorInt int transparentColor;\n"
		+ "  @BindView(android.R.id.button1) View button1;\n"
		+ "  public C(View view) {\n"
		+ "    super(view);\n"
		+ "  }\n"
		+ "}\n");

	JavaFileObject sourceD = JavaFileObjects.forSourceString(
		"test.D",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.ColorInt;\n"
		+ "import butterknife.BindColor;\n"
		+ "public class D extends C {\n"
		+
		"  @BindColor(android.R.color.darker_gray) @ColorInt int grayColor;\n"
		+ "  public D(View view) {\n"
		+ "    super(view);\n"
		+ "  }\n"
		+ "}\n");

	JavaFileObject sourceE = JavaFileObjects.forSourceString(
		"test.E",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.ColorInt;\n"
		+ "import butterknife.BindColor;\n"
		+ "public class E extends C {\n"
		+
		"  @BindColor(android.R.color.background_dark) @ColorInt int backgroundDarkColor;\n"
		+ "  public E(View view) {\n"
		+ "    super(view);\n"
		+ "  }\n"
		+ "}\n");

	JavaFileObject sourceF = JavaFileObjects.forSourceString(
		"test.F",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.ColorInt;\n"
		+ "import butterknife.BindColor;\n"
		+ "public class F extends D {\n"
		+
		"  @BindColor(android.R.color.background_light) @ColorInt int backgroundLightColor;\n"
		+ "  public F(View view) {\n"
		+ "    super(view);\n"
		+ "  }\n"
		+ "}\n");

	JavaFileObject sourceG = JavaFileObjects.forSourceString(
		"test.G",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.ColorInt;\n"
		+ "import butterknife.BindView;\n"
		+ "import butterknife.BindColor;\n"
		+ "import butterknife.OnClick;\n"
		+ "public class G extends E {\n"
		+
		"  @BindColor(android.R.color.darker_gray) @ColorInt int grayColor;\n"
		+ "  @BindView(android.R.id.button2) View button2;\n"
		+ "  public G(View view) {\n"
		+ "    super(view);\n"
		+ "  }\n"
		+ "  @OnClick(android.R.id.content) public void onClick() {\n"
		+ "  }\n"
		+ "}\n");

	JavaFileObject sourceH = JavaFileObjects.forSourceString(
		"test.H",
		""
		+ "package test;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.ColorInt;\n"
		+ "import butterknife.BindView;\n"
		+ "import butterknife.BindColor;\n"
		+ "public class H extends G {\n"
		+
		"  @BindColor(android.R.color.holo_green_dark) @ColorInt int holoGreenDark;\n"
		+ "  @BindView(android.R.id.button3) View button3;\n"
		+ "  public H(View view) {\n"
		+ "    super(view);\n"
		+ "  }\n"
		+ "}\n");

	JavaFileObject bindingASource = JavaFileObjects.forSourceString(
		"test/A_ViewBinding",
		""
		+ "// Generated code from Butter Knife. Do not modify!\n"
		+ "package test;\n"
		+ "import android.content.Context;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.CallSuper;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import androidx.core.content.ContextCompat;\n"
		+ "import butterknife.Unbinder;\n"
		+ "import java.lang.Deprecated;\n"
		+ "import java.lang.Override;\n"
		+ "public class A_ViewBinding implements Unbinder {\n"
		+ "  /**\n"
		+
		"   * @deprecated Use {@link #Test_ViewBinding(A, Context)} for direct creation.\n"
		+
		"   *     Only present for runtime invocation through {@code ButterKnife.bind()}.\n"
		+ "   */\n"
		+ "  @Deprecated\n"
		+ "  @UiThread\n"
		+ "  public Test_ViewBinding(A target, View source) {\n"
		+ "    this(target, source.getContext());\n"
		+ "  }\n"
		+ "  @UiThread\n"
		+ "  public A_ViewBinding(A target, Context context) {\n"
		+
		"    target.blackColor = ContextCompat.getColor(context, android.R.color.black);\n"
		+ "  }\n"
		+ "  @Override\n"
		+ "  @CallSuper\n"
		+ "  public void unbind() {\n"
		+ "  }\n"
		+ "}");

	JavaFileObject bindingBSource = JavaFileObjects.forSourceString(
		"test/B_ViewBinding",
		""
		+ "// Generated code from Butter Knife. Do not modify!\n"
		+ "package test;\n"
		+ "import android.content.Context;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import androidx.core.content.ContextCompat;\n"
		+ "import java.lang.Deprecated;\n"
		+ "public class B_ViewBinding extends A_ViewBinding {\n"
		+ "  /**\n"
		+
		"   * @deprecated Use {@link #Test_ViewBinding(B, Context)} for direct creation.\n"
		+
		"   *     Only present for runtime invocation through {@code ButterKnife.bind()}.\n"
		+ "   */\n"
		+ "  @Deprecated\n"
		+ "  @UiThread\n"
		+ "  public Test_ViewBinding(B target, View source) {\n"
		+ "    this(target, source.getContext());\n"
		+ "  }\n"
		+ "  @UiThread\n"
		+ "  public B_ViewBinding(B target, Context context) {\n"
		+ "    super(target, context);\n"
		+
		"    target.whiteColor = ContextCompat.getColor(context, android.R.color.white);\n"
		+ "  }\n"
		+ "}");

	JavaFileObject bindingCSource = JavaFileObjects.forSourceString(
		"test/C_ViewBinding",
		""
		+ "// Generated code from Butter Knife. Do not modify!\n"
		+ "package test;\n"
		+ "import android.content.Context;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import androidx.core.content.ContextCompat;\n"
		+ "import butterknife.internal.Utils;\n"
		+ "import java.lang.IllegalStateException;\n"
		+ "import java.lang.Override;\n"
		+ "public class C_ViewBinding extends B_ViewBinding {\n"
		+ "  private C target;\n"
		+ "  @UiThread\n"
		+ "  public C_ViewBinding(C target, View source) {\n"
		+ "    super(target, source.getContext());\n"
		+ "    this.target = target;\n"
		+
		"    target.button1 = Utils.findRequiredView(source, android.R.id.button1, \"field 'button1'\");\n"
		+ "    Context context = source.getContext();\n"
		+
		"    target.transparentColor = ContextCompat.getColor(context, android.R.color.transparent);\n"
		+ "  }\n"
		+ "  @Override\n"
		+ "  public void unbind() {\n"
		+ "    C target = this.target;\n"
		+
		"    if (target == null) throw new IllegalStateException(\"Bindings already cleared.\");\n"
		+ "    this.target = null;\n"
		+ "    target.button1 = null;\n"
		+ "    super.unbind();\n"
		+ "  }\n"
		+ "}");

	JavaFileObject bindingDSource = JavaFileObjects.forSourceString(
		"test/D_ViewBinding",
		""
		+ "package test;\n"
		+ "import android.content.Context;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import androidx.core.content.ContextCompat;\n"
		+ "public class D_ViewBinding extends C_ViewBinding {\n"
		+ "  @UiThread\n"
		+ "  public D_ViewBinding(D target, View source) {\n"
		+ "    super(target, source);\n"
		+ "    Context context = source.getContext();\n"
		+
		"    target.grayColor = ContextCompat.getColor(context, android.R.color.darker_gray);\n"
		+ "  }\n"
		+ "}");

	JavaFileObject bindingESource = JavaFileObjects.forSourceString(
		"test/E_ViewBinding",
		""
		+ "package test;\n"
		+ "import android.content.Context;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import androidx.core.content.ContextCompat;\n"
		+ "public class E_ViewBinding extends C_ViewBinding {\n"
		+ "  @UiThread\n"
		+ "  public E_ViewBinding(E target, View source) {\n"
		+ "    super(target, source);\n"
		+ "    Context context = source.getContext();\n"
		+
		"    target.backgroundDarkColor = ContextCompat.getColor(context, android.R.color.background_dark);\n"
		+ "  }\n"
		+ "}");

	JavaFileObject bindingFSource = JavaFileObjects.forSourceString(
		"test/F_ViewBinding",
		""
		+ "package test;\n"
		+ "import android.content.Context;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import androidx.core.content.ContextCompat;\n"
		+ "public class F_ViewBinding extends D_ViewBinding {\n"
		+ "  @UiThread\n"
		+ "  public F_ViewBinding(F target, View source) {\n"
		+ "    super(target, source);\n"
		+ "    Context context = source.getContext();\n"
		+
		"    target.backgroundLightColor = ContextCompat.getColor(context, android.R.color.background_light);\n"
		+ "  }\n"
		+ "}");

	JavaFileObject bindingGSource = JavaFileObjects.forSourceString(
		"test/G_ViewBinding",
		""
		+ "package test;\n"
		+ "import android.content.Context;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import androidx.core.content.ContextCompat;\n"
		+ "import butterknife.internal.DebouncingOnClickListener;\n"
		+ "import butterknife.internal.Utils;\n"
		+ "import java.lang.IllegalStateException;\n"
		+ "import java.lang.Override;\n"
		+ "public class G_ViewBinding extends E_ViewBinding {\n"
		+ "  private G target;\n"
		+ "  private View view1020002;\n"
		+ "  @UiThread\n"
		+ "  public G_ViewBinding(final G target, View source) {\n"
		+ "    super(target, source);\n"
		+ "    this.target = target;\n"
		+ "    View view;\n"
		+
		"    target.button2 = Utils.findRequiredView(source, android.R.id.button2, \"field 'button2'\");\n"
		+
		"    view = Utils.findRequiredView(source, android.R.id.content, \"method 'onClick'\");\n"
		+ "    view1020002 = view;\n"
		+ "    view.setOnClickListener(new DebouncingOnClickListener() {\n"
		+ "      @Override\n"
		+ "      public void doClick(View p0) {\n"
		+ "        target.onClick();\n"
		+ "      }\n"
		+ "    });\n"
		+ "    Context context = source.getContext();\n"
		+
		"    target.grayColor = ContextCompat.getColor(context, android.R.color.darker_gray);\n"
		+ "  }\n"
		+ "  @Override\n"
		+ "  public void unbind() {\n"
		+ "    G target = this.target;\n"
		+
		"    if (target == null) throw new IllegalStateException(\"Bindings already cleared.\");\n"
		+ "    this.target = null\n"
		+ "    target.button2 = null;\n"
		+ "    view1020002.setOnClickListener(null);\n"
		+ "    view1020002 = null;\n"
		+ "    super.unbind();\n"
		+ "  }\n"
		+ "}");

	JavaFileObject bindingHSource = JavaFileObjects.forSourceString(
		"test/H_ViewBinding",
		""
		+ "package test;\n"
		+ "import android.content.Context;\n"
		+ "import android.view.View;\n"
		+ "import androidx.annotation.UiThread;\n"
		+ "import androidx.core.content.ContextCompat;\n"
		+ "import butterknife.internal.Utils;\n"
		+ "import java.lang.IllegalStateException;\n"
		+ "import java.lang.Override;\n"
		+ "public class H_ViewBinding extends G_ViewBinding {\n"
		+ "  private H target;\n"
		+ "  @UiThread\n"
		+ "  public H_ViewBinding(H target, View source) {\n"
		+ "    super(target, source);\n"
		+ "    this.target = target;\n"
		+
		"    target.button3 = Utils.findRequiredView(source, android.R.id.button3, \"field 'button3'\");\n"
		+ "    Context context = source.getContext();\n"
		+
		"    target.holoGreenDark = ContextCompat.getColor(context, android.R.color.holo_green_dark);\n"
		+ "  }\n"
		+ "  @Override\n"
		+ "  public void unbind() {\n"
		+ "    H target = this.target;\n"
		+
		"    if (target == null) throw new IllegalStateException(\"Bindings already cleared.\");\n"
		+ "    this.target = null;\n"
		+ "    target.button3 = null;\n"
		+ "    super.unbind();\n"
		+ "  }\n"
		+ "}");

	assertAbout(javaSources())
	.that(asList(sourceA, sourceB, sourceC, sourceD, sourceE, sourceF,
	             sourceG, sourceH))
	.withCompilerOptions("-Xlint:-processing")
	.processedWith(new ButterKnifeProcessor())
	.compilesWithoutWarnings()
	.and()
	.generatesSources(bindingASource, bindingBSource, bindingCSource,
	                  bindingDSource, bindingESource, bindingFSource,
	                  bindingGSource, bindingHSource);
}
}
