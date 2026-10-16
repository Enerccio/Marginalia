// The red bar that warns about invalid extensions (nothing is changed). Pictures: invalid-extensions-warning.
// State: a COPY of 13-db-backup in which the Author's Note JAR was replaced by the broken one (bin/make-broken-plugin.py)
// and its report deleted before the start, see the block in bin/shoot-all.sh.
import {idle, login, main, snap} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await idle(page, 2500);
  // the top of the workspace: the bar above the tabs
  await snap(page, 'invalid-extensions-warning', {});
});
