import T from '../../../../l10n';

export interface MenuItem {
  id: string,
  title: string,
}

export const menuItems: MenuItem[] = [
  { id: 'word-manager', title: T`Words Manager` },
  { id: 'learning-settings', title: T`Learning Settings` },
]

export const defaultMenuItem : MenuItem = menuItems[1]!;
