### Added

- EMI's tag tabs are now split into dedicated category tabs for Item Tags, Block Tags, Fluid Tags, and Entity Tags.
- Entity type tags are now registered in EMI and displayed when right-clicking spawn eggs in the index.
- Searching with the `#` tag prefix or `r#` now supports searching entity type tags and fluid tags (e.g.,
  `#skeletons`).
- Added support for creating and toggling stack groups based on block tags directly from tag recipe pages.
- Added config options for the new tag tabs.

### Fixed

- Fixed an EMI bug where the block tags tab was missing due to unregistered block adapters.