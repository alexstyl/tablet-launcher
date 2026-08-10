# Split Screen API Reference

This document describes the split-screen launch behavior verified on a BOOX Go
10.3 running Android 12 (SDK API level 32). It is device-specific guidance,
not a public Android API contract.

## Scope and limitations

Android applications do not have a public SDK API for placing arbitrary apps
into split-screen panes. The commands below use the privileged ADB shell
interface to Android's activity manager. They will not work from an ordinary
installed Android application without shell/system privileges.

BOOX also exposes a proprietary chooser intent, but no supported API was found
for passing an app pair to it directly.

## App integration investigation and outcome

We spiked an in-app launcher flow that opened a separate, resizable activity
before invoking BOOX's chooser. The secondary activity was placed in its own
standard task, rather than the launcher Home task, and BOOX correctly reported
it as resizable. That did not change the chooser's behavior: selecting an app
from the chooser opened it normally instead of placing the caller and selected
app into split screen.

The BOOX system tray was also inspected. Its split-screen action emits this
private broadcast after split mode is operated:

```text
onyx.action.user.operate.split.handle
```

This is a notification, not a command that opens the split-screen UI. Sending
the broadcast from the launcher reaches BOOX receivers but does not enter
split-screen mode. It also cannot be started as an activity: no activity
resolves this action.

The actual system-tray flow uses BOOX's privileged SystemUI split controller.
It is not exposed through a public Android API, a launchable BOOX activity, or
an app-pair intent extra. The app integration spike was therefore removed.
The ADB shell commands remain the only proven way to launch two known apps
directly into their respective panes on this device.

Making the launcher a system app is not practical on a stock device. It would
require BOOX's platform signing key and installation in the system/privileged
partition, and would still depend on private SystemUI APIs.

## Capability checks

The BOOX Go 10.3 reports both capabilities as enabled:

```sh
adb shell cmd activity supports-multiwindow
adb shell cmd activity supports-split-screen-multi-window
```

Both commands return `true` on the tested device.

## Windowing modes

`cmd activity start-activity` accepts `--windowingMode`:

| Value | Mode |
| --- | --- |
| `3` | Split-screen primary pane |
| `4` | Split-screen secondary pane |

On a landscape display, primary is the left pane and secondary is the right
pane. In portrait, the same pair is rendered top then bottom. Rotation is
enabled on the tested device, so rotating it to landscape changes the layout to
left/right while preserving the app assignment.

## Tested app launch components

| App | Package | Launcher component |
| --- | --- | --- |
| Amazon Kindle | `com.amazon.kindle` | `com.amazon.kindle/.UpgradePage` |
| Google Keep | `com.google.android.keep` | `com.google.android.keep/.activities.BrowseActivity` |

These components and the apps' resizable declarations were verified from the
installed packages on the BOOX Go 10.3.

## Kindle left, Keep right

Start Kindle in the primary pane, then start Keep in the secondary pane:

```sh
adb shell cmd activity start-activity --windowingMode 3 \
  -a android.intent.action.MAIN \
  -c android.intent.category.LAUNCHER \
  -n com.amazon.kindle/.UpgradePage

adb shell cmd activity start-activity --windowingMode 4 \
  -a android.intent.action.MAIN \
  -c android.intent.category.LAUNCHER \
  -n com.google.android.keep/.activities.BrowseActivity
```

For the reverse arrangement, launch Keep with mode `3` and Kindle with mode
`4`.

## BOOX chooser intent

BOOX provides an exported activity for its interactive multi-window chooser:

```sh
adb shell am start -a onyx.action.START_MULTI_WINDOW_CHOOSER
```

This opens BOOX's UI for selecting apps. It is proprietary and does not expose
a documented extra or intent parameter that launches a known app pair.

## Verification

Inspect the active split panes with:

```sh
adb shell cmd activity stack list
```

Expected output includes separate root tasks with
`split-screen-primary` and `split-screen-secondary` windowing modes. On the
tested device, the Kindle and Keep task bounds each occupy one half of the
display.
