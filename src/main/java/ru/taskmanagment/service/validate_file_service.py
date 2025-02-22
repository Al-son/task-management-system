import subprocess
import truffleHog

def check_dependencies():
    print("Running dependency check...")
    try:
        subprocess.run(["pip", "check"], check=True)
        print("Dependency check passed.")
    except subprocess.CalledProcessError:
        print("Dependency check failed. Please ensure all dependencies are installed.")
        return False
    return True


def static_code_analysis():
    print("Running static code analysis...")
    try:
        subprocess.run(["flake8", "."], check=True)
        subprocess.run(["bandit", "-r", "."], check=True)
        print("Static code analysis passed.")
    except subprocess.CalledProcessError:
        print("Static code analysis failed. Please fix the reported issues.")
        return False
    return True


def sensitive_data_check():
    print("Scanning for sensitive data...")
    try:
        truffleHog.find_strings(".", output_json="sensitive_data.json")
        print("Sensitive data check passed.")
    except Exception as e:
        print(f"Sensitive data check failed: {e}")
        return False
    return True


def run_tests_and_coverage():
    print("Running tests and generating coverage report...")
    try:
        subprocess.run(["pytest", "--cov=validate_file_service", "--cov-report=html"], check=True)
        print("Tests passed and coverage report generated.")
    except subprocess.CalledProcessError:
        print("Tests failed or coverage verification failed.")
        return False
    return True


def main_pipeline():
    stages = [
        ("Dependency Checking", check_dependencies),
        ("Static Code Analysis", static_code_analysis),
        ("Sensitive Data Checking", sensitive_data_check),
        ("Run Tests and Coverage Verification", run_tests_and_coverage),
    ]

    for stage_name, stage_function in stages:
        print(f"Starting stage: {stage_name}")
        if not stage_function():
            print(f"Pipeline stopped at stage: {stage_name}")
            return

    print("Pipeline completed successfully!")

if __name__ == "__main__":
    main_pipeline()