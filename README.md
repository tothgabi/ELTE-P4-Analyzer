# ELTE_P4_Analyzer

Copyright © 2021 ELTE IK

## Current Contents

**The goal of this research is to create an analyzer framework for P4 programs.**

In this version of our tool, we only show the verification analysis.

## Verification

### Theoretical results
The theoretical background of the research has already published as a short paper in the _The 12th Conference of PhD Students in
Computer Science: Volume of short papers_: [Gabriella Tóth, Máté Tejfel: Component-based error detection of P4 programs](http://inf.u-szeged.hu/~cscs/pdf/cscs2020.pdf#page=85)

The main idea is to calculate pre- and postconditions of the _actions_, _tables_ and _controls_ ; and to find errors and suspicious cases in the
input program which can be caused by the usage of invalid header and uninitialized fields.

## Usage

The repository root can be imported as a folder in Visual Studio Code.

Target is Java 8.

Recommended extensions for VSCode: Java Extension Pack (Microsoft), Language Support for Java (Red Hat), Maven for Java (Microsoft).

It has to be launched by the main class: _p4analyser.broker.App_

**Starting from VSCode:** the _.vscode/launch.json_ has already contained the arguments of the launching, therefore the default launch setting is _verify_ with the default 
_basic.p4_. If someone would like to try it with another file, then it can be given in the json file, as a second argument. 

## Contributors
**Máté Tejfel** - supervisor

**Dániel Lukács** - theoretical idea and implementation of the tool

**Gabriella Tóth** - theoretical idea and implementation of the verification

_Eötvös Loránd University, Faculty of Informatics, Department of Programming Languages and Compilers_
