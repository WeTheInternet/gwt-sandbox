package com.google.gwt.dev.resource.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class PathMatcher {

    /** @deprecated */
    protected static final String[] DEFAULTEXCLUDES = new String[]{"**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*", "**/CVS", "**/CVS/**", "**/.cvsignore", "**/SCCS", "**/SCCS/**", "**/vssver.scc", "**/.svn", "**/.svn/**", "**/.DS_Store"};
    private static Vector defaultExcludes = new Vector();
    protected String[] includes;
    protected String[] excludes;
    protected boolean isCaseSensitive = true;
    private Set includeNonPatterns = new HashSet();
    private Set excludeNonPatterns = new HashSet();
    private String[] includePatterns;
    private String[] excludePatterns;
    private boolean areNonPatternSetsReady = false;


    private static String[] tokenizePathAsArray(String path) {
        char sep = File.separatorChar;
        int start = 0;
        int len = path.length();
        int count = 0;

        for(int pos = 0; pos < len; ++pos) {
            if(path.charAt(pos) == sep) {
                if(pos != start) {
                    ++count;
                }

                start = pos + 1;
            }
        }

        if(len != start) {
            ++count;
        }

        String[] l = new String[count];
        count = 0;
        start = 0;

        String tok;
        for(int pos1 = 0; pos1 < len; ++pos1) {
            if(path.charAt(pos1) == sep) {
                if(pos1 != start) {
                    tok = path.substring(start, pos1);
                    l[count++] = tok;
                }

                start = pos1 + 1;
            }
        }

        if(len != start) {
            tok = path.substring(start);
            l[count] = tok;
        }

        return l;
    }

    public static boolean matchPath(String pattern, String str, boolean isCaseSensitive) {
        if(str.startsWith(File.separator) != pattern.startsWith(File.separator)) {
            return false;
        } else {
            String[] patDirs = tokenizePathAsArray(pattern);
            String[] strDirs = tokenizePathAsArray(str);
            int patIdxStart = 0;
            int patIdxEnd = patDirs.length - 1;
            int strIdxStart = 0;

            int strIdxEnd;
            String i;
            for(strIdxEnd = strDirs.length - 1; patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd; ++strIdxStart) {
                i = patDirs[patIdxStart];
                if(i.equals("**")) {
                    break;
                }

                if(!match(i, strDirs[strIdxStart], isCaseSensitive)) {
                    patDirs = null;
                    strDirs = null;
                    return false;
                }

                ++patIdxStart;
            }

            int var18;
            if(strIdxStart > strIdxEnd) {
                for(var18 = patIdxStart; var18 <= patIdxEnd; ++var18) {
                    if(!patDirs[var18].equals("**")) {
                        patDirs = null;
                        strDirs = null;
                        return false;
                    }
                }

                return true;
            } else if(patIdxStart > patIdxEnd) {
                patDirs = null;
                strDirs = null;
                return false;
            } else {
                while(patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
                    i = patDirs[patIdxEnd];
                    if(i.equals("**")) {
                        break;
                    }

                    if(!match(i, strDirs[strIdxEnd], isCaseSensitive)) {
                        patDirs = null;
                        strDirs = null;
                        return false;
                    }

                    --patIdxEnd;
                    --strIdxEnd;
                }

                if(strIdxStart > strIdxEnd) {
                    for(var18 = patIdxStart; var18 <= patIdxEnd; ++var18) {
                        if(!patDirs[var18].equals("**")) {
                            patDirs = null;
                            strDirs = null;
                            return false;
                        }
                    }

                    return true;
                } else {
                    while(patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
                        var18 = -1;

                        for(int i1 = patIdxStart + 1; i1 <= patIdxEnd; ++i1) {
                            if(patDirs[i1].equals("**")) {
                                var18 = i1;
                                break;
                            }
                        }

                        if(var18 == patIdxStart + 1) {
                            ++patIdxStart;
                        } else {
                            int patLength = var18 - patIdxStart - 1;
                            int strLength = strIdxEnd - strIdxStart + 1;
                            int foundIdx = -1;
                            int i2 = 0;

                            label118:
                            while(i2 <= strLength - patLength) {
                                for(int j = 0; j < patLength; ++j) {
                                    String subPat = patDirs[patIdxStart + j + 1];
                                    String subStr = strDirs[strIdxStart + i2 + j];
                                    if(!match(subPat, subStr, isCaseSensitive)) {
                                        ++i2;
                                        continue label118;
                                    }
                                }

                                foundIdx = strIdxStart + i2;
                                break;
                            }

                            if(foundIdx == -1) {
                                patDirs = null;
                                strDirs = null;
                                return false;
                            }

                            patIdxStart = var18;
                            strIdxStart = foundIdx + patLength;
                        }
                    }

                    for(var18 = patIdxStart; var18 <= patIdxEnd; ++var18) {
                        if(!patDirs[var18].equals("**")) {
                            patDirs = null;
                            strDirs = null;
                            return false;
                        }
                    }

                    return true;
                }
            }
        }
    }

    public static boolean match(String pattern, String str) {
        return match(pattern, str, true);
    }

    public static boolean match(String pattern, String str, boolean isCaseSensitive) {
        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        boolean containsStar = false;

        for(int i = 0; i < patArr.length; ++i) {
            if(patArr[i] == 42) {
                containsStar = true;
                break;
            }
        }

        char ch;
        int i1;
        if(!containsStar) {
            if(patIdxEnd != strIdxEnd) {
                return false;
            } else {
                for(i1 = 0; i1 <= patIdxEnd; ++i1) {
                    ch = patArr[i1];
                    if(ch != 63) {
                        if(isCaseSensitive && ch != strArr[i1]) {
                            return false;
                        }

                        if(!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[i1])) {
                            return false;
                        }
                    }
                }

                return true;
            }
        } else if(patIdxEnd == 0) {
            return true;
        } else {
            while((ch = patArr[patIdxStart]) != 42 && strIdxStart <= strIdxEnd) {
                if(ch != 63) {
                    if(isCaseSensitive && ch != strArr[strIdxStart]) {
                        return false;
                    }

                    if(!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxStart])) {
                        return false;
                    }
                }

                ++patIdxStart;
                ++strIdxStart;
            }

            if(strIdxStart > strIdxEnd) {
                for(i1 = patIdxStart; i1 <= patIdxEnd; ++i1) {
                    if(patArr[i1] != 42) {
                        return false;
                    }
                }

                return true;
            } else {
                while((ch = patArr[patIdxEnd]) != 42 && strIdxStart <= strIdxEnd) {
                    if(ch != 63) {
                        if(isCaseSensitive && ch != strArr[strIdxEnd]) {
                            return false;
                        }

                        if(!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxEnd])) {
                            return false;
                        }
                    }

                    --patIdxEnd;
                    --strIdxEnd;
                }

                if(strIdxStart > strIdxEnd) {
                    for(i1 = patIdxStart; i1 <= patIdxEnd; ++i1) {
                        if(patArr[i1] != 42) {
                            return false;
                        }
                    }

                    return true;
                } else {
                    while(patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
                        i1 = -1;

                        for(int i2 = patIdxStart + 1; i2 <= patIdxEnd; ++i2) {
                            if(patArr[i2] == 42) {
                                i1 = i2;
                                break;
                            }
                        }

                        if(i1 == patIdxStart + 1) {
                            ++patIdxStart;
                        } else {
                            int patLength = i1 - patIdxStart - 1;
                            int strLength = strIdxEnd - strIdxStart + 1;
                            int foundIdx = -1;

                            label172:
                            for(int i3 = 0; i3 <= strLength - patLength; ++i3) {
                                for(int j = 0; j < patLength; ++j) {
                                    ch = patArr[patIdxStart + j + 1];
                                    if(ch != 63 && (isCaseSensitive && ch != strArr[strIdxStart + i3 + j] || !isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxStart + i3 + j]))) {
                                        continue label172;
                                    }
                                }

                                foundIdx = strIdxStart + i3;
                                break;
                            }

                            if(foundIdx == -1) {
                                return false;
                            }

                            patIdxStart = i1;
                            strIdxStart = foundIdx + patLength;
                        }
                    }

                    for(i1 = patIdxStart; i1 <= patIdxEnd; ++i1) {
                        if(patArr[i1] != 42) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }
    }

    public static String[] getDefaultExcludes() {
        return (String[])defaultExcludes.toArray(new String[defaultExcludes.size()]);
    }

    public static void resetDefaultExcludes() {
        defaultExcludes = new Vector();

        for(int i = 0; i < DEFAULTEXCLUDES.length; ++i) {
            defaultExcludes.add(DEFAULTEXCLUDES[i]);
        }

    }

    public synchronized boolean isCaseSensitive() {
        return this.isCaseSensitive;
    }

    public synchronized void setIncludes(String[] includes) {
        if(includes == null) {
            this.includes = null;
        } else {
            this.includes = new String[includes.length];

            for(int i = 0; i < includes.length; ++i) {
                this.includes[i] = normalizePattern(includes[i]);
            }
        }

    }

    public synchronized void setExcludes(String[] excludes) {
        if(excludes == null) {
            this.excludes = null;
        } else {
            this.excludes = new String[excludes.length];

            for(int i = 0; i < excludes.length; ++i) {
                this.excludes[i] = normalizePattern(excludes[i]);
            }
        }

    }

    private static String normalizePattern(String p) {
        String pattern = p.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        if(pattern.endsWith(File.separator)) {
            pattern = pattern + "**";
        }

        return pattern;
    }

    protected boolean isIncluded(String name) {
        this.ensureNonPatternSetsReady();
        if(this.isCaseSensitive()) {
            if(this.includeNonPatterns.contains(name)) {
                return true;
            }
        } else if(this.includeNonPatterns.contains(name.toUpperCase())) {
            return true;
        }

        for(int i = 0; i < this.includePatterns.length; ++i) {
            if(matchPath(this.includePatterns[i], name, this.isCaseSensitive())) {
                return true;
            }
        }

        return false;
    }

    protected boolean isExcluded(String name) {
        this.ensureNonPatternSetsReady();
        if(this.isCaseSensitive()) {
            if(this.excludeNonPatterns.contains(name)) {
                return true;
            }
        } else if(this.excludeNonPatterns.contains(name.toUpperCase())) {
            return true;
        }

        for(int i = 0; i < this.excludePatterns.length; ++i) {
            if(matchPath(this.excludePatterns[i], name, this.isCaseSensitive())) {
                return true;
            }
        }

        return false;
    }

    public synchronized void addDefaultExcludes() {
        int excludesLength = this.excludes == null?0:this.excludes.length;
        String[] newExcludes = new String[excludesLength + defaultExcludes.size()];
        if(excludesLength > 0) {
            System.arraycopy(this.excludes, 0, newExcludes, 0, excludesLength);
        }

        String[] defaultExcludesTemp = getDefaultExcludes();

        for(int i = 0; i < defaultExcludesTemp.length; ++i) {
            newExcludes[i + excludesLength] = defaultExcludesTemp[i].replace('/', File.separatorChar).replace('\\', File.separatorChar);
        }

        this.excludes = newExcludes;
    }

    private synchronized void ensureNonPatternSetsReady() {
        if(!this.areNonPatternSetsReady) {
            this.includePatterns = this.fillNonPatternSet(this.includeNonPatterns, this.includes);
            this.excludePatterns = this.fillNonPatternSet(this.excludeNonPatterns, this.excludes);
            this.areNonPatternSetsReady = true;
        }

    }

    private String[] fillNonPatternSet(Set set, String[] patterns) {
        ArrayList al = new ArrayList(patterns.length);

        for(int i = 0; i < patterns.length; ++i) {
            if(!hasWildcards(patterns[i])) {
                set.add(this.isCaseSensitive()?patterns[i]:patterns[i].toUpperCase());
            } else {
                al.add(patterns[i]);
            }
        }

        return set.size() == 0?patterns:(String[])al.toArray(new String[al.size()]);
    }

    public static boolean hasWildcards(String input) {
        return input.indexOf(42) != -1 || input.indexOf(63) != -1;
    }

    public void init() {
        if(this.includes == null) {
            this.includes = new String[1];
            this.includes[0] = "**";
        }

        if(this.excludes == null) {
            this.excludes = new String[0];
        }

    }

    public boolean match(String path) {
        String vpath = path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        return this.isIncluded(vpath) && !this.isExcluded(vpath);
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.isCaseSensitive = caseSensitive;
    }

    public PathMatcher() {
    }

    static {
        resetDefaultExcludes();
    }
}
