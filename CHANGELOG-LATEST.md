### Added

- Added new config options:
    - **Disable Pagination Wrapping**: Disables wrapping around to the first/last page.
    - **Enable Scroll Mode**: Disable pages and scroll through items one row at a time instead.
    - **Show Page Title in Headers**: Hides page numbers from headers and shows the title of the page instead. If using
      creative tabs, the active creative tab title is used for the Index page.
    - **Hide Single Sidebar Switch Button**: Hides the sidebar page switching button if there's only one page in the
      sidebar.
    - **Incremental Scrollbar Fill**: Fills the sidebar scrollbar from left to right as you scroll or page through,
      instead of showing a discrete chunk (RRV fans wya?).
- Added a warning that stack group modification is only possible while inside a world.

### Changed

- Switched to YACL instead of modifying EMI's config screen.

### Fixed

- Fixed tabs not displaying under certain conditions.
- Fixed desync between enabled/disabled stack groups in the config screen and the tag button.