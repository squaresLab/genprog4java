# README #

To run GenProg on Defects4j bugs with invariant diversity, use this: https://github.com/squaresLab/GenProgScripts/tree/ZhenYiweiInvariantAnalysis

To do the same on IntroClassJava bugs, use this:
https://github.com/squaresLab/GenProgInvDiv-IntroClassJava-Scripts

We manually refactored a subset of IntroClassJava to relocate the bulk of program logic into a separate method. Our refactored IntroClassJava bugs are stored in the following fork of IntroClassJava:
https://github.com/squaresLab/IntroClassJava

This extension of GenProg4Java is currently only compatible with Java code stored inside of packages that all have the same outermost name. For example, `com.example.a` and `com.example.b` is fine, but `com.example.a` and `org.example.a` will break GP4J+Invariants.
