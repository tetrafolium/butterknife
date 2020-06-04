package butterknife.lint;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public final class LintRegistryTest {
@Test
public void issues() {
	assertThat(new LintRegistry().getIssues())
	.contains(InvalidR2UsageDetector.ISSUE);
}
}
