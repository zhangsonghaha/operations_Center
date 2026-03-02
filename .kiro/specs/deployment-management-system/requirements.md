# Requirements Document

## Introduction

This document specifies the requirements for a Deployment Management System built on the RuoYi enterprise platform (Spring Boot 2.5.15 + Vue 3). The system manages application deployments to remote servers via SSH, providing automated environment detection, configuration management, port conflict resolution, and real-time deployment monitoring.

The system addresses common deployment challenges in enterprise environments, particularly Java application deployments where environment variables may not be available in non-interactive SSH sessions, configuration files need to be managed separately from application packages, and port conflicts must be resolved automatically.

## Glossary

- **Deployment_System**: The application deployment management system being specified
- **SSH_Session**: A Secure Shell connection established to a remote server for executing deployment commands
- **Java_Environment**: The Java runtime environment including the java executable and related environment variables
- **Configuration_File**: External Spring Boot configuration files (YAML or properties format) used to override application settings
- **Deployment_Script**: Shell scripts that control application lifecycle (start, stop, restart)
- **Application_Package**: The deployable artifact (JAR, WAR, or other format) to be deployed to remote servers
- **Port_Conflict**: A condition where the configured application port is already in use by another process
- **Deployment_Log**: A record of deployment execution including output, errors, and status
- **JVM_Parameters**: Java Virtual Machine startup parameters (e.g., -Xms, -Xmx, -XX:+UseG1GC)
- **Spring_Boot_Parameters**: Application-level parameters passed to Spring Boot (e.g., --server.port, --spring.profiles.active)
- **WebSocket_Stream**: A bidirectional communication channel for streaming real-time log data to the UI
- **Non_Interactive_Session**: An SSH session without a pseudo-terminal where environment variables may not be loaded automatically

## Requirements

### Requirement 1: Java Environment Auto-Detection

**User Story:** As a deployment operator, I want the system to automatically detect Java in non-interactive SSH sessions, so that deployments succeed even when environment variables are not loaded.

#### Acceptance Criteria

1. WHEN a deployment script is executed, THE Deployment_System SHALL load environment variables from /etc/profile, ~/.bash_profile, and ~/.bashrc
2. WHEN environment variables are loaded, THE Deployment_System SHALL attempt to locate the java executable using the which command
3. IF the which command fails to locate java, THEN THE Deployment_System SHALL search common installation paths including /usr/bin/java, /usr/local/bin/java, /usr/local/java/bin/java, /usr/java/latest/bin/java, and /opt/java/bin/java
4. WHEN a java executable is found, THE Deployment_System SHALL verify it is executable and display its version information
5. IF no java executable is found, THEN THE Deployment_System SHALL terminate the deployment with an error message indicating Java is not available
6. THE Deployment_System SHALL replace all java command references in user scripts with the detected Java path

### Requirement 2: External Configuration File Support

**User Story:** As a deployment operator, I want to specify external configuration files for different environments, so that I can manage environment-specific settings separately from the application package.

#### Acceptance Criteria

1. THE Deployment_System SHALL provide a configuration file path field accepting file paths or directory paths
2. WHEN a configuration file path is provided, THE Deployment_System SHALL validate that the path exists on the remote server before deployment
3. WHEN the configuration path is a file, THE Deployment_System SHALL verify it has a .yml, .yaml, or .properties extension
4. WHEN the configuration path is a directory, THE Deployment_System SHALL list configuration files within that directory
5. WHEN a valid configuration path exists, THE Deployment_System SHALL append --spring.config.additional-location=file:{path} to the java command
6. THE Deployment_System SHALL use spring.config.additional-location to preserve JAR-internal configuration while adding external configuration

### Requirement 3: Dynamic Port Configuration

**User Story:** As a deployment operator, I want the system to extract port numbers from Spring Boot parameters and update deployment scripts automatically, so that port configuration is centralized and consistent.

#### Acceptance Criteria

1. THE Deployment_System SHALL provide a Spring Boot parameters field for application-level parameters
2. WHEN Spring Boot parameters contain --server.port=N or -Dserver.port=N, THE Deployment_System SHALL extract the port number N
3. WHEN a port number is extracted, THE Deployment_System SHALL replace PORT=xxxx declarations in deployment scripts with PORT=N
4. WHEN Spring Boot parameters are provided, THE Deployment_System SHALL append them after the -jar {jarfile} argument
5. THE Deployment_System SHALL preserve the order of parameters: JVM parameters, -jar, JAR filename, Spring Boot parameters

### Requirement 4: Automatic Port Conflict Resolution

**User Story:** As a deployment operator, I want the system to automatically detect and resolve port conflicts, so that deployments do not fail due to ports already in use.

#### Acceptance Criteria

1. WHEN an application is starting, THE Deployment_System SHALL check if the configured port is already in use using lsof or netstat
2. IF the configured port is occupied, THEN THE Deployment_System SHALL identify the process ID occupying the port
3. WHEN a port conflict is detected, THE Deployment_System SHALL attempt to terminate the conflicting process using kill -15
4. WHEN graceful termination fails after 5 seconds, THE Deployment_System SHALL force-kill the process using kill -9
5. WHEN the port is freed, THE Deployment_System SHALL proceed with application startup
6. THE Deployment_System SHALL log all port conflict resolution actions to the deployment log

### Requirement 5: Separated Parameter Management

**User Story:** As a deployment operator, I want to manage JVM parameters and Spring Boot parameters separately, so that I can configure infrastructure and application concerns independently.

#### Acceptance Criteria

1. THE Deployment_System SHALL provide a JVM parameters field for Java Virtual Machine configuration
2. THE Deployment_System SHALL provide a Spring Boot parameters field for application configuration
3. WHEN generating deployment scripts, THE Deployment_System SHALL place JVM parameters immediately after the java command
4. WHEN generating deployment scripts, THE Deployment_System SHALL place Spring Boot parameters after the JAR filename
5. THE Deployment_System SHALL construct commands in the format: java {JVM_params} -jar {jarfile} {Spring_params}
6. THE Deployment_System SHALL allow either parameter field to be empty without affecting script generation

### Requirement 6: Visual Configuration Generator

**User Story:** As a deployment operator, I want to generate deployment scripts through a visual interface, so that I can create correct scripts without manual shell scripting knowledge.

#### Acceptance Criteria

1. THE Deployment_System SHALL provide a visual configuration interface with application type selection
2. WHEN the application type is Spring Boot JAR, THE Deployment_System SHALL provide fields for JAR filename, JVM memory settings, GC algorithm, Spring profile, and server port
3. WHEN the application type is Node.js, THE Deployment_System SHALL provide fields for entry file, process manager, and instance count
4. WHEN the application type is Python, THE Deployment_System SHALL provide fields for WSGI application, web server, and worker count
5. WHEN the application type is Docker, THE Deployment_System SHALL provide fields for image name, container name, and port mapping
6. WHEN the user clicks "Generate Scripts", THE Deployment_System SHALL create start and stop scripts based on the visual configuration
7. THE Deployment_System SHALL allow users to edit generated scripts in an integrated code editor

### Requirement 7: Deployment Log System

**User Story:** As a deployment operator, I want to view real-time deployment logs, so that I can monitor deployment progress and troubleshoot failures immediately.

#### Acceptance Criteria

1. WHEN a deployment is initiated, THE Deployment_System SHALL create a deployment log record with status "running"
2. THE Deployment_System SHALL stream deployment output to the log record in real-time using WebSocket
3. WHEN deployment output is received from the SSH session, THE Deployment_System SHALL append it to the log content within 100 milliseconds
4. WHEN the deployment completes successfully, THE Deployment_System SHALL update the log status to "success" and record the end time
5. IF the deployment fails, THEN THE Deployment_System SHALL update the log status to "failed", record the error message, and record the end time
6. THE Deployment_System SHALL display deployment logs in the UI with automatic scrolling to show the latest output
7. THE Deployment_System SHALL preserve deployment logs for historical review and audit purposes

### Requirement 8: SSH Connection Management

**User Story:** As a deployment operator, I want the system to manage SSH connections reliably, so that deployments execute securely and connection resources are properly released.

#### Acceptance Criteria

1. WHEN a deployment is initiated, THE Deployment_System SHALL establish an SSH connection using JSch library with the configured server credentials
2. THE Deployment_System SHALL set SSH connection timeout to 30 seconds
3. THE Deployment_System SHALL disable strict host key checking to allow connections to new servers
4. WHEN SSH commands are executed, THE Deployment_System SHALL use pseudo-terminal mode to ensure output is not buffered
5. WHEN a deployment completes or fails, THE Deployment_System SHALL disconnect all SSH channels and sessions
6. THE Deployment_System SHALL log SSH connection establishment and disconnection events

### Requirement 9: Application Package Upload

**User Story:** As a deployment operator, I want to upload application packages through the UI, so that I can deploy applications without manual file transfer.

#### Acceptance Criteria

1. THE Deployment_System SHALL provide a file upload interface accepting JAR, WAR, ZIP, and TAR formats
2. THE Deployment_System SHALL limit uploaded file size to 500 megabytes
3. WHEN a file is uploaded successfully, THE Deployment_System SHALL store it in the profile upload directory and record the file path
4. WHEN a deployment is executed, THE Deployment_System SHALL transfer the application package to the remote server using SFTP
5. THE Deployment_System SHALL create remote directories if they do not exist before uploading files
6. THE Deployment_System SHALL log file upload progress including file size and transfer status

### Requirement 10: Deployment Script Enhancement

**User Story:** As a deployment operator, I want the system to enhance my deployment scripts automatically, so that environment detection and configuration injection work without manual script modification.

#### Acceptance Criteria

1. WHEN a user provides a deployment script, THE Deployment_System SHALL prepend environment detection logic
2. THE Deployment_System SHALL inject Java path detection code before executing user script commands
3. WHEN a configuration file path is configured, THE Deployment_System SHALL inject configuration path validation and parameter construction
4. WHEN Spring Boot parameters are configured, THE Deployment_System SHALL inject them into java -jar commands
5. THE Deployment_System SHALL replace standalone java commands with ${JAVA_CMD} variable references
6. THE Deployment_System SHALL preserve user script logic while adding enhancement wrappers

### Requirement 11: Multi-Server Deployment Support

**User Story:** As a deployment operator, I want to associate applications with multiple servers, so that I can deploy the same application to different environments.

#### Acceptance Criteria

1. THE Deployment_System SHALL allow users to associate multiple servers with an application
2. WHEN initiating a deployment, THE Deployment_System SHALL display only the servers associated with the selected application
3. THE Deployment_System SHALL require the user to select a target server before executing deployment
4. THE Deployment_System SHALL execute deployments to one server at a time
5. THE Deployment_System SHALL create separate deployment log records for each server deployment

### Requirement 12: Deployment Type Operations

**User Story:** As a deployment operator, I want to perform different deployment operations (deploy, start, stop, restart), so that I can manage application lifecycle without writing custom scripts.

#### Acceptance Criteria

1. THE Deployment_System SHALL support deployment type "deploy" which uploads the package and executes the start script
2. THE Deployment_System SHALL support deployment type "start" which executes only the start script
3. THE Deployment_System SHALL support deployment type "stop" which executes the stop script
4. THE Deployment_System SHALL support deployment type "restart" which executes stop, waits 2 seconds, then executes start
5. WHEN no stop script is configured, THE Deployment_System SHALL use a default stop method that kills processes by application name
6. THE Deployment_System SHALL log the deployment type at the beginning of each deployment operation

### Requirement 13: Script Execution Timeout

**User Story:** As a deployment operator, I want deployment scripts to timeout if they hang, so that deployments do not block indefinitely.

#### Acceptance Criteria

1. THE Deployment_System SHALL monitor script execution for output activity
2. WHEN no output is received for 60 seconds, THE Deployment_System SHALL terminate the script execution
3. WHEN a timeout occurs, THE Deployment_System SHALL log a timeout warning message
4. THE Deployment_System SHALL record the exit status of executed scripts
5. IF a script exits with a non-zero status, THEN THE Deployment_System SHALL mark the deployment as failed

### Requirement 14: Configuration Validation

**User Story:** As a deployment operator, I want the system to validate configuration before deployment, so that I receive early feedback on configuration errors.

#### Acceptance Criteria

1. WHEN an application is saved, THE Deployment_System SHALL validate that the application name contains only Chinese characters, English letters, numbers, and underscores
2. THE Deployment_System SHALL validate that the application name is between 2 and 50 characters
3. THE Deployment_System SHALL validate that the deployment path is an absolute path (starting with / on Linux or drive letter on Windows)
4. THE Deployment_System SHALL validate that monitor ports are numbers between 1 and 65535
5. THE Deployment_System SHALL validate that multiple ports are separated by commas
6. WHEN a start script is required, THE Deployment_System SHALL prevent saving if the start script field is empty

### Requirement 15: Deployment History and Audit

**User Story:** As a system administrator, I want to review deployment history, so that I can audit who deployed what and when.

#### Acceptance Criteria

1. THE Deployment_System SHALL record the executor username for each deployment
2. THE Deployment_System SHALL record the start time and end time for each deployment
3. THE Deployment_System SHALL preserve the complete log content for each deployment
4. THE Deployment_System SHALL allow users to search deployment logs by application name, server name, deployment type, and status
5. THE Deployment_System SHALL display deployment duration calculated from start and end times

## Parser and Serializer Requirements

This system does not include parsers or serializers that require round-trip testing. Configuration files are handled by Spring Boot's built-in configuration system, and deployment scripts are plain text shell scripts that are not parsed by the Deployment_System itself.

## Notes

- The system relies on SSH and SFTP protocols for remote server communication
- Java environment detection is critical for non-interactive SSH sessions where .bashrc may not be sourced
- The --spring.config.additional-location parameter preserves JAR-internal configuration while adding external configuration, unlike --spring.config.location which replaces it
- Port conflict resolution uses graceful termination (SIGTERM) before forced termination (SIGKILL) to allow applications to shut down cleanly
- WebSocket streaming provides real-time log updates without polling overhead
- The visual configuration generator reduces deployment script errors by providing validated templates
