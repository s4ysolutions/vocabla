import React from 'react'

export interface MenuItem {
    id: string,
    title: string,
}

export const menuItems: MenuItem[] = [
    { id: 'word-manager', title: 'Words Manager' },
    { id: 'learning-settings', title: 'Learning Settings' },
    { id: 'menu2', title: 'Menu 2' },
    { id: 'menu3', title: 'Menu 3' },
]

interface Props {
    selectedMenu: MenuItem,
    setSelectedMenu: (menu: MenuItem) => void,
    setIsMenuOpen: (isOpen: boolean) => void
}

const MeAppMenu: React.FC<Props> = ({selectedMenu, setSelectedMenu, setIsMenuOpen}) => {

  return (
    <ul className="space-y-2">
      {menuItems.map((item) => (
        <li
          key={item.id}
          className={`cursor-pointer p-2 rounded ${
            selectedMenu === item ? 'bg-blue-500 text-white' : 'hover:bg-gray-300'
          }`}
          onClick={() => {
            setSelectedMenu(item)
            setIsMenuOpen(false) // Close menu on selection
          }}
        >
          {item.title}
        </li>
      ))}
    </ul>
  )
}

export default MeAppMenu