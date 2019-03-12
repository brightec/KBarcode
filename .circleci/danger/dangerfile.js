// Import the feedback functions
import { danger, fail, markdown, schedule, warn, message } from "danger"

// The Danger DSL can be a bit verbose, so let's rename
const modified = danger.git.modified_files
const newFiles = danger.git.created_files
// Modified or Created can be treated the same a lot of the time
const touchedFiles = modified.concat(newFiles)

// Get some commonly used elements out of the DSL
const pr = danger.github.pr
const bodyAndTitle = (pr.body + pr.title).toLowerCase()

// Custom modifiers for people submitting PRs to be able to say "skip this"
const trivialPR = bodyAndTitle.includes("#trivial")

// Rules
const enableRuleSetChangelogRule = false
const enableRuleSetLicense = false

// When there are app-changes and the PR is not marked as #trivial, expect
// there to be CHANGELOG changes.
if (enableRuleSetChangelogRule) {
  const changelogChanges = modified.find(f => f === "CHANGELOG.md")
  const changelogFileChanged = changelogChanges !== undefined
  if (touchedFiles.length > 0 && !trivialPR && !changelogFileChanged) {
    warn("This PR does not include a CHANGELOG entry.")
  }
}

// If there are any changes to app or data build.gradle files, check if the license.html file changed
if (enableRuleSetLicense) {
  const app_build_updated = modified.find(f => f === "app/build.gradle")
  const data_build_updated = modified.find(f => f === "data/build.gradle")
  const licenses_updated = modified.find(f => f === "app/src/main/assets/third_party_licenses.html")

  if (touchedFiles.length > 0 && (app_build_updated !== undefined || data_build_updated !== undefined)) {
    warn("The app or data build.gradle files were updated, but there were no changes in the list of licenses. Did you forget to update the third_party_licenses.html file?")
  }
}
