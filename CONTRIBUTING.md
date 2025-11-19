
# Contributing to CBTS Enterprise (Backend)

Thank you for considering contributing to the **CBTS Enterprise Backend**! We value your ideas, code, and feedback to make this Java (Spring Boot) service the most robust and maintainable it can be.

> **Note:**  
> If you want to contribute to the Angular frontend, please see the [cbts-enterprise-frontend](https://github.com/Almubarak96/cbts-enterprise-frontend) repository and its own CONTRIBUTING.md.

---

## How to Contribute

1. **Fork the Repository**
   - Fork this backend repository on GitHub, then clone your fork locally.

2. **Create a Feature Branch**
   - Make a new branch for your changes:  
     `git checkout -b feature/my-new-feature`

3. **Make Your Changes**
   - Write clear, maintainable, well-tested Java code.
   - Follow the code style conventions described below.
   - Update or add documentation where relevant (JavaDoc, comments, or markdown files).

4. **Test Your Code**
   - Ensure all tests pass:  
     `mvn test`
   - Add new unit/integration tests for new features or bugfixes.

5. **Commit and Push**
   - Write informative commit messages:  
     `git commit -m "Add feature: ..."`
   - Push your branch:  
     `git push origin feature/my-new-feature`

6. **Open a Pull Request (PR)**
   - In GitHub, open a PR from your fork’s branch to the `main` branch here.
   - Describe clearly what your PR implements, changes, or fixes.
   - Reference relevant issues if applicable.

---

## Code Style Guidelines

- **Java (Spring Boot):**
  - Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) or existing repository conventions.
  - Name variables, methods, and classes meaningfully.
  - Use consistent indentation and formatting.
  - Document public classes and complex methods with JavaDoc/comments.
  - Write modular, reusable, and testable code.

- **Database & Integration:**
  - If your contributions require changes to MySQL or Redis integration, ensure configuration is clear and compatible.
  - Add/modify migration scripts if the database schema changes.

## Pull Request Review Process

- All PRs are reviewed for clarity, code quality, adherence to style, and test coverage.
- Be ready to revise code if feedback is provided—review discussion is encouraged and welcomed.
- Use squash merges or clean up commit history if asked.

---

## Reporting Issues

- Use [GitHub Issues](https://github.com/Almubarak96/cbts-enterprise/issues) for bugs, questions, or feature requests related to the backend.
- When possible, include logs, stack traces, and steps to reproduce.

---

## Communication & Conduct

- Keep all interactions respectful and constructive.
- For sensitive matters, contact the backend maintainer at almubaraksuleiman96@example.com.

Thank you for being part of the CBTS Enterprise backend community!

---

*Adapted and inspired by open source best practices.*
