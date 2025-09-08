import React from 'react'
import {type MenuItem, menuItems} from './menuItems.ts';

interface Props {
  selectedMenu: MenuItem,
  setSelectedMenu: (menu: MenuItem) => void,
  setIsMenuOpen: (isOpen: boolean) => void
}

const MeAppMenu: React.FC<Props> = ({ selectedMenu, setSelectedMenu, setIsMenuOpen }) => {

  return (
    <ul className="space-y-2">
      {menuItems.map((item) => (
        <li
          key={item.id}
          className={`cursor-pointer p-2 rounded ${selectedMenu === item ? 'bg-blue-500 text-white' : 'hover:bg-gray-300'
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
