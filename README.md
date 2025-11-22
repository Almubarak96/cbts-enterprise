# CBTS Enterprise

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat&logo=java&logoColor=white)](https://java.com)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1+-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io)
[![Angular](https://img.shields.io/badge/Angular-15+-DD0031?style=flat&logo=angular&logoColor=white)](https://angular.io)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat&logo=mysql&logoColor=white)](https://mysql.com)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)](https://redis.io)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Support](https://img.shields.io/badge/Support-Paystack-00B4B4)](https://paystack.shop/pay/pz8t31-07-)

CBTS Enterprise is a robust, full-stack solution designed to modernize and streamline business operations. Featuring a scalable and secure Java backend, a performant Angular frontend, a reliable MySQL database, and Redis for real-time exam and timer features, this project sets a strong foundation for enterprise-grade CBT (Computer-Based Testing) and more.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture Overview](#architecture-overview)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Features

- **Full-Stack Architecture:** Java backend + Angular frontend for seamless and modern UX.
- **Enterprise-Ready:** Built for scalability, reliability, and real-time interaction.
- **Security First:** Adheres to industry best practices for authentication and data protection.
- **Advanced CBT Management:** CBT timer, test instructions, acknowledgment, and session data using Redis for instant feedback and performance.
- **Robust Data Layer:** Uses MySQL for reliable data storage.
- **Developer Friendly:** Structured project, clear documentation, and integrated API docs via Swagger UI.
- **Extensible:** Modular design for rapid feature additions and maintenance.

## Technology Stack

| Layer     | Tech                  | Description                                         |
|-----------|-----------------------|-----------------------------------------------------|
| Backend   | Java (Spring Boot)    | REST APIs, business logic, system integrations      |
| Frontend  | Angular               | Responsive, progressive web application             |
| Database  | MySQL                 | Stores persistent business and exam data            |
| Caching   | Redis                 | Real-time CBT timer and test acknowledgment         |
| API Docs  | Swagger UI            | Interactive documentation for RESTful API           |

- **Backend Repo:** [cbts-enterprise (Java)](https://github.com/Almubarak96/cbts-enterprise)
- **Frontend Repo:** [cbts-enterprise-frontend (Angular)](https://github.com/Almubarak96/cbts-enterprise-frontend)

## Architecture Overview

```
[ Angular Frontend ] <-> [ Java Backend (REST API) ] <-> [ MySQL Database ]
                                            |
                                   [ Redis: Session, Timers, Acks ]
```

- The **Angular frontend** provides a user-centric interface.
- The **Java backend** exposes REST APIs, handles enterprise logic, connects to MySQL, and uses Redis for fast, transient data such as timers and acknowledgments.

## Getting Started

### Prerequisites

- [Java 17+](https://adoptopenjdk.net/)
- [Node.js & npm (for Angular frontend)](https://nodejs.org/)
- [Angular CLI](https://angular.io/cli)
- [MySQL](https://www.mysql.com/)
- [Redis](https://redis.io/)
- [Maven or Gradle]
- (Optional) Docker

### Backend Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/Almubarak96/cbts-enterprise.git
   cd cbts-enterprise
   ```
2. **Configure MySQL and Redis:**
   Edit `src/main/resources/application.properties` with your own MySQL and Redis credentials and connection info.

3. **Build and Run:**
   ```bash
   mvn clean install
   java -jar target/cbts-enterprise.jar
   ```

4. **API Documentation (Swagger UI):**
   Once running, explore and test REST APIs at:  
   [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)

### Frontend Setup

1. **Clone the Angular Frontend:**
   ```bash
   git clone https://github.com/Almubarak96/cbts-enterprise-frontend.git
   cd cbts-enterprise-frontend
   ```
2. **Install dependencies and serve:**
   ```bash
   npm install
   ng serve
   ```
   Visit [http://localhost:4200](http://localhost:4200)

## Usage

- Configure API endpoints, MySQL, and Redis settings in your environment files.
- Use Swagger UI ([http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)) to interactively explore and test backend APIs.
- Review the `docs/` directory for extended guides and system diagrams.

## Support This Project

If CBTS Enterprise helps your organization or learning journey, consider supporting its continued development and maintenance.

### Quick Online Support
[![Support via Paystack](https://img.shields.io/badge/💸_Support_OpenSource-Paystack-00B4B4?style=for-the-badge)](https://paystack.shop/pay/pz8t31-07-)

### International Supporters Welcome!
The payment page automatically shows local currency (USD, EUR, GBP) for international supporters.

### Direct Bank Transfer (Nigeria)
Email **almubaraksuleiman96@gmail.com** for direct bank transfer details.

### What Your Support Enables
- More enterprise features and enhancements
- Comprehensive documentation and tutorials
- Server infrastructure and hosting costs
- Security updates and maintenance
- Support for educational institutions

**Join the mission to make enterprise-grade software accessible to all!** 

## Contributing

We welcome community contributions!  
To propose changes, please review and follow the [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Licensed under the MIT License. See the [LICENSE](LICENSE.md) for more information.

## Contact

- **Maintainer:** [Almubarak96](https://github.com/Almubarak96)
- **Email:** almubaraksuleiman96@gmail.com

---

> **Empowering enterprise innovation with scalable, real-time, full-stack solutions.**
