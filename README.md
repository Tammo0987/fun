# Fun

## Table of Contents

- [Overview](#overview)
- [Description](#description)
- [Prerequisites](#prerequisites)
- [How to Build](#how-to-build)
- [How to Run](#how-to-run)

## Description

Fun is an experimental programming language designed to explore innovative solutions for modern software development
challenges. It is primarily **functional**, with a **blend of object-oriented programming** concepts to enhance
flexibility. Simplification is at its core—each problem has only one **language feature** to address it, avoiding
unnecessary complexity.

### Key Aspects:

- **Functional-first approach**: Prioritizes pure functions while allowing controlled use of OOP for structure and
  modularity.
- **Single feature per problem**: Encourages clean, intuitive code by providing one solution for each problem type.
- **Focus on security and side effects**: Built with secure coding practices in mind, emphasizing predictability and
  accountability for side effects.
- **Runs on the JVM**: Fully Java-compatible without compromising security principles, making it easy to integrate with
  existing Java-based systems.

This language showcases an elegant and secure way to approach coding, offering a modern perspective while ensuring
interoperability within the Java ecosystem.

## Overview

This project is built using Gradle, a powerful build automation tool, with a wrapper included in the repository to
simplify usage. It is designed to be modular, flexible, and scalable to suit various requirements.

## Prerequisites

- **Java**: Ensure you have Java 21 installed.

## How to Build

1. Clone the repository:
   ```bash
   git clone https://github.com/Tammo0987/fun.git
   ```
2. Navigate to the project directory:
   ```bash
   cd fun
   ```
3. Build the project using the included Gradle Wrapper:
   ```bash
   ./gradlew build   # On Linux/MacOS
   gradlew.bat build # On Windows
   ```

## How to Run

Once the project is built, run the application with:

```bash
./gradlew run   # On Linux/MacOS
gradlew.bat run # On Windows
```