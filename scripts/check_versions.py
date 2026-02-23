#!/usr/bin/env python3
"""
Version checker for Telegram variants.
Checks GitHub releases and APKMirror for new versions.
Outputs JSON and creates GitHub issues for updates.
"""

import json
import os
import re
import sys
from datetime import datetime
from pathlib import Path

import requests

# Telegram variants to track
TELEGRAM_VARIANTS = {
    "it.octogram.android": {
        "name": "Octogram",
        "github": "OctogramApp/Octogram",
        "apkmirror": "octogram",
        "package_name": "it.octogram.android",
    },
    "org.forkgram.messenger": {
        "name": "Forkgram",
        "github": "Forkgram/TelegramAndroid",
        "apkmirror": "forkgram/forkgram-f-droid-version",
        "package_name": "org.forkgram.messenger",
    },
    "org.telegram.messenger": {
        "name": "Telegram (Official)",
        "github": None,
        "apkmirror": "telegram-fz-llc/telegram",
        "package_name": "org.telegram.messenger",
    },
    "org.telegram.messenger.beta": {
        "name": "Telegram Beta",
        "github": None,
        "apkmirror": "telegram-fz-llc/telegram-beta",
        "package_name": "org.telegram.messenger.beta",
    },
    "org.telegram.messenger.web": {
        "name": "Telegram Web",
        "github": None,
        "apkmirror": "telegram-fz-llc/telegram-web-version",
        "package_name": "org.telegram.messenger.web",
    },
    "org.telegram.plus": {
        "name": "Plus Messenger",
        "github": None,
        "apkmirror": "rafalense/plus-messenger",
        "package_name": "org.telegram.plus",
    },
    "tw.nekomimi.nekogram": {
        "name": "Nekogram",
        "github": "Nekogram/Nekogram",
        "apkmirror": "mimiworks/nekogram",
        "package_name": "tw.nekomimi.nekogram",
    },
    "uz.unnarsx.cherrygram": {
        "name": "Cherrygram",
        "github": "arsLan4k1390/Cherrygram",
        "apkmirror": "cherrygram",
        "package_name": "uz.unnarsx.cherrygram",
    },
    "xyz.nextalone.nagram": {
        "name": "Nagram",
        "github": "NextAlone/Nagram",
        "apkmirror": "nagram",
        "package_name": "xyz.nextalone.nagram",
    },
}


class VersionChecker:
    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({"User-Agent": "Telegami-Version-Checker/1.0"})
        self.github_token = os.environ.get("GITHUB_TOKEN")
        self.results = {}

    def get_github_release(self, repo: str):
        """Get latest release version from GitHub."""
        if not repo:
            return None

        url = f"https://api.github.com/repos/{repo}/releases/latest"
        headers = {}
        if self.github_token:
            headers["Authorization"] = f"token {self.github_token}"

        try:
            response = self.session.get(url, headers=headers, timeout=10)
            response.raise_for_status()
            data = response.json()

            version = data.get("tag_name", "").lstrip("v")
            release_url = data.get("html_url", "")

            return (version, release_url) if version else None
        except requests.RequestException as e:
            print(f"Error fetching GitHub release for {repo}: {e}")
            return None

    def get_apkmirror_version(self, app_slug):
        """Get latest version from APKMirror RSS feed."""
        if not app_slug:
            return None

        # RSS feed URL format: https://www.apkmirror.com/apk/{app_slug}/feed/
        rss_url = f"https://www.apkmirror.com/apk/{app_slug}/feed/"

        try:
            response = self.session.get(rss_url, timeout=10)
            response.raise_for_status()

            # Parse RSS XML
            # Look for version in <title> tags, format: "AppName x.x.x by Developer"
            # Example: "Telegram 12.4.3 by Telegram FZ-LLC"
            pattern = (
                r"<title>([^<]+?) ([0-9]+(?:\.[0-9]+)+(?:\.[0-9]+)?) by [^<]+</title>"
            )

            match = re.search(pattern, response.text)
            if match:
                version = match.group(2)
                return (version, f"https://www.apkmirror.com/apk/{app_slug}/")

            return None
        except requests.RequestException as e:
            print(f"Error fetching APKMirror RSS for {app_slug}: {e}")
            return None

    def get_current_version(self, package_name: str):
        """Get current version from tracking file."""
        tracking_file = Path("versions.json")
        if tracking_file.exists():
            with open(tracking_file) as f:
                data = json.load(f)
                return data.get(package_name, {}).get("current_version")
        return None

    def check_variant(self, package_name: str, info):
        """Check version for a single variant."""
        result = {
            "name": info["name"],
            "package_name": package_name,
            "current_version": self.get_current_version(package_name) or "Unknown",
            "latest_version": "Unknown",
            "latest_version_url": "",
            "source": "none",
            "update_available": False,
            "last_checked": datetime.utcnow().isoformat() + "Z",
        }

        # Try GitHub first
        if info.get("github"):
            github_result = self.get_github_release(info["github"])
            if github_result:
                result["latest_version"] = github_result[0]
                result["latest_version_url"] = github_result[1]
                result["source"] = "github"

        # Fallback to APKMirror
        if result["latest_version"] == "Unknown" and info.get("apkmirror"):
            apkmirror_result = self.get_apkmirror_version(info["apkmirror"])
            if apkmirror_result:
                result["latest_version"] = apkmirror_result[0]
                result["latest_version_url"] = apkmirror_result[1]
                result["source"] = "apkmirror"

        # Check if update is available
        if (
            result["current_version"] != "Unknown"
            and result["latest_version"] != "Unknown"
        ):
            # Simple string comparison (assumes semantic versioning)
            if result["latest_version"] != result["current_version"]:
                result["update_available"] = True

        return result

    def run(self):
        """Run version check for all variants."""
        print("Checking versions for Telegram variants...")

        for package_name, info in TELEGRAM_VARIANTS.items():
            print(f"Checking {info['name']} ({package_name})...")
            self.results[package_name] = self.check_variant(package_name, info)

        return self.results

    def save_results(self, output_file: str = "versions.json"):
        """Save results to JSON file."""
        with open(output_file, "w") as f:
            json.dump(self.results, f, indent=2)
        print(f"Results saved to {output_file}")

    def create_github_issues(self, repo: str):
        """Create GitHub issues for available updates."""
        if not self.github_token:
            print("No GITHUB_TOKEN provided, skipping issue creation")
            return

        for package_name, result in self.results.items():
            if not result["update_available"]:
                continue

            # Check if issue already exists
            issue_title = f"[Update] {result['name']}: {result['current_version']} → {result['latest_version']}"

            existing_issues = self.get_existing_issues(repo, issue_title)
            if existing_issues:
                print(f"Issue already exists for {result['name']}, skipping...")
                continue

            # Create new issue
            issue_body = f"""## New Version Available

**App:** {result["name"]}
**Package:** `{package_name}`
**Current Version:** {result["current_version"]}
**Latest Version:** {result["latest_version"]}
**Source:** {result["source"]}

### Download Links
- [Latest Release]({result["latest_version_url"]})

### Check Details
- **Last Checked:** {result["last_checked"]}
- **Update Status:** {"⚠️ Update Available" if result["update_available"] else "✅ Up to Date"}

---
*This issue was automatically created by the version checker.*
"""

            self.create_issue(repo, issue_title, issue_body)
            print(f"Created issue for {result['name']}")

    def get_existing_issues(self, repo: str, title: str):
        """Check if an issue with the exact same version update already exists."""
        url = f"https://api.github.com/repos/{repo}/issues"
        headers = {"Authorization": f"token {self.github_token}"}

        try:
            response = self.session.get(
                url,
                headers=headers,
                params={"state": "open", "labels": "version-update"},
                timeout=10,
            )
            response.raise_for_status()
            issues = response.json()

            # Extract app name and version from the new title
            # Format: "[Update] AppName: current_version → latest_version"
            title_match = re.match(r"\[Update\] (.+): (.+) → (.+)", title)
            if not title_match:
                return []

            new_app = title_match.group(1)
            new_current = title_match.group(2)
            new_latest = title_match.group(3)

            # Check for exact version match in existing issues
            for issue in issues:
                issue_title = issue.get("title", "")
                issue_match = re.match(r"\[Update\] (.+): (.+) → (.+)", issue_title)
                if issue_match:
                    existing_app = issue_match.group(1)
                    existing_current = issue_match.group(2)
                    existing_latest = issue_match.group(3)

                    # Only skip if same app AND same version transition
                    if (
                        existing_app == new_app
                        and existing_current == new_current
                        and existing_latest == new_latest
                    ):
                        return [issue]
            return []
        except requests.RequestException as e:
            print(f"Error checking existing issues: {e}")
            return []

    def create_issue(self, repo: str, title: str, body: str):
        """Create a new GitHub issue."""
        url = f"https://api.github.com/repos/{repo}/issues"
        headers = {"Authorization": f"token {self.github_token}"}

        data = {
            "title": title,
            "body": body,
            "labels": ["version-update", "automated"],
        }

        try:
            response = self.session.post(url, headers=headers, json=data, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"Error creating issue: {e}")
            return None


def main():
    """Main entry point."""
    checker = VersionChecker()

    # Run version checks
    results = checker.run()

    # Save results
    checker.save_results("versions.json")

    # Print summary
    print("\n" + "=" * 60)
    print("VERSION CHECK SUMMARY")
    print("=" * 60)

    updates_available = 0
    for package_name, result in results.items():
        status = "🔄 UPDATE" if result["update_available"] else "✅ OK"
        print(
            f"{status} {result['name']}: {result['current_version']} → {result['latest_version']}"
        )
        if result["update_available"]:
            updates_available += 1

    print("=" * 60)
    print(
        f"Total: {len(results)} variants checked, {updates_available} updates available"
    )

    # Create GitHub issues if requested
    if os.environ.get("CREATE_ISSUES") == "true":
        repo = os.environ.get("GITHUB_REPOSITORY")
        if repo:
            print(f"\nCreating GitHub issues for {repo}...")
            checker.create_github_issues(repo)
        else:
            print("\nNo GITHUB_REPOSITORY set, skipping issue creation")

    # Exit with error code if updates found (for CI/CD)
    if updates_available > 0 and os.environ.get("FAIL_ON_UPDATE") == "true":
        sys.exit(1)


if __name__ == "__main__":
    main()
