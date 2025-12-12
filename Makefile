.PHONY: deps format format-check compile clean run kill

deps: ## Check for dependency updates
	sbt dependencyUpdates

format: ## Format all code with scalafmt
	sbt scalafmtAll scalafmtSbt

format-check: ## Check if code is formatted properly
	sbt scalafmtCheckAll scalafmtSbtCheck

compile: ## Compile the project
	sbt compile

test: ## Run unit tests
	sbt test

clean: ## Remove all target directories
	rm -rf target project/target

run: ## Run the application
	sbt run
