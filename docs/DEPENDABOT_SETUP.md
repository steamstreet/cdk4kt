# Dependabot Configuration for CDK4KT

This document explains the automated dependency management setup for the CDK4KT project.

## Overview

The project uses GitHub's Dependabot to automatically update dependencies, with special handling for AWS CDK updates
that require code generation validation.

## Configuration Files

### `.github/dependabot.yml`

Configures Dependabot to:

- **Weekly CDK Updates**: Check for AWS CDK updates every Monday at 9 AM
- **Grouped Updates**: Group related dependencies (AWS CDK, Kotlin, build tools) for easier review
- **Major Version Protection**: Flag CDK major version updates for manual review
- **Auto-assignment**: Assign updates to the CDK4KT maintainers team

### `.github/workflows/cdk-update-test.yml`

Validates CDK updates by:

- **Code Generation Testing**: Ensures critical construct wrappers are generated
- **Breaking Change Detection**: Compares generated code between versions
- **Compatibility Reporting**: Provides detailed PR comments with validation results
- **Lambda Extension Testing**: Verifies that Lambda.kt extensions compile with new wrappers

### `.github/workflows/build.yml`

Enhanced build workflow that:

- **Validates Code Generation**: Checks that core construct functions exist
- **Runs on PRs**: Tests all pull requests including Dependabot updates
- **Uploads Artifacts**: Saves generated wrappers for debugging

## Update Process

### Automatic Updates

1. **Dependabot Detection**: Dependabot detects new CDK versions weekly
2. **PR Creation**: Creates a pull request with the version update
3. **Automated Testing**:
    - Builds the project with the new CDK version
    - Validates that code generation still works
    - Tests that critical construct wrappers are generated
    - Compiles Lambda extensions to ensure compatibility
4. **Reporting**: Adds detailed comments to the PR with validation results
5. **Review**: Maintainers review and merge if tests pass

### Manual Review Required

Major CDK version updates (e.g., 2.x → 3.x) are flagged for manual review because they may:

- Introduce breaking changes in the CDK API
- Require updates to the KCDKBuilder logic
- Need new handling for construct patterns

## Key Validations

### Critical Construct Functions

The automation validates that these essential construct functions are generated:

- `Construct.Queue()` - SQS queue creation
- `Construct.Alarm()` - CloudWatch alarm creation
- `Construct.Rule()` - EventBridge rule creation

### Breaking Change Detection

Compares generated wrapper files between CDK versions to identify:

- Removed functions or parameters
- Changed function signatures
- New constructs or deprecations

## Troubleshooting

### Failed Code Generation

If code generation fails after a CDK update:

1. Check the workflow logs for specific errors
2. Look for new CDK patterns that aren't handled by KCDKBuilder
3. Update the builder logic in `buildSrc/src/main/kotlin/KCDKBuilder.kt`
4. Add new validation rules if needed

### Missing Construct Functions

If critical construct functions aren't generated:

1. Verify the construct still exists in the new CDK version
2. Check if AWS changed the constructor pattern
3. Update the `isConstruct` logic in KCDKBuilder if needed
4. Add new constructs to the validation list

### Test Failures

If Lambda extension tests fail:

1. Check if generated function signatures changed
2. Update import statements if package names changed
3. Verify that the DSL patterns still work

## Maintenance

### Adding New Critical Constructs

To monitor additional constructs in the validation:

1. Add checks in `.github/workflows/cdk-update-test.yml`:

```bash
if ! grep -q "fun Construct.NewConstruct(" "$WRAPPERS_DIR/path/to/service.kt"; then
  echo "❌ NewConstruct function missing"
  exit 1
fi
```

2. Add the same check to `.github/workflows/build.yml`

### Updating Dependabot Configuration

To modify update frequency or grouping:

1. Edit `.github/dependabot.yml`
2. Adjust schedule, grouping, or ignore patterns as needed
3. See [Dependabot documentation](https://docs.github.com/en/code-security/dependabot) for options

## Security

- Dependabot PRs are automatically tested but not auto-merged for security
- Major version updates require manual review to prevent breaking changes
- All updates go through the same CI validation as manual PRs
- Generated code is validated to prevent injection of malicious patterns

## Benefits

1. **Early Detection**: Catch CDK compatibility issues immediately
2. **Automated Testing**: Comprehensive validation without manual effort
3. **Detailed Reporting**: Clear feedback on what changed and whether it works
4. **Reduced Maintenance**: Automatic updates for minor/patch versions
5. **Breaking Change Protection**: Manual review gates for major updates