package root.core.codecompletion;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassNameMatcherTest {

    @Test
    public void shouldMatchAndNotMatchSomeClasses(){
        ClassNameMatcher classNameMatcher = new ClassNameMatcher();
        ClassMatchType b1 = classNameMatcher.doesClassNameMatch("FileEncodingChecker", "FEC");
        ClassMatchType b3 = classNameMatcher.doesClassNameMatch("FileEncodingChecker", "ncodin");
        ClassMatchType b4 = classNameMatcher.doesClassNameMatch("FileEncodingChecker", "FileEncodingChecker");
        ClassMatchType b5 = classNameMatcher.doesClassNameMatch("FileEncodingChecker", "fileencodingchecker");
        ClassMatchType b6 = classNameMatcher.doesClassNameMatch("BasicAuthentication", "BasAuth");

        assertThat(b1.isMatch()).isTrue();
        assertThat(b3.isMatch()).isTrue();
        assertThat(b4.isMatch()).isTrue();
        assertThat(b5.isMatch()).isTrue();
        assertThat(b6.isMatch()).isTrue();

        ClassMatchType n1 = classNameMatcher.doesClassNameMatch("FileEncodingCheckerExplorer", "FE");
        ClassMatchType n2 = classNameMatcher.doesClassNameMatch("FileEncodingCheckerExplorer", "FECEP");
        ClassMatchType n3 = classNameMatcher.doesClassNameMatch("FileEncodingCheckerExplorer", "FileEenc");
        assertThat(n1.isMatch()).isFalse();
        assertThat(n2.isMatch()).isFalse();
        assertThat(n3.isMatch()).isFalse();
    }

}