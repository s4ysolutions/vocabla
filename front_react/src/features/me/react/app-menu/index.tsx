import React from 'react'

interface Props {
    selectedMenu: string,
    setSelectedMenu: (menu: string) => void,
    setIsMenuOpen: (isOpen: boolean) => void
}

const MeAppMenu: React.FC<Props> = ({selectedMenu, setSelectedMenu, setIsMenuOpen}) => {
  const menuItems = ['Menu 1', 'Menu 2', 'Menu 3']

  return (
    <ul className="space-y-2">
      {menuItems.map((item) => (
        <li
          key={item}
          className={`cursor-pointer p-2 rounded ${
            selectedMenu === item ? 'bg-blue-500 text-white' : 'hover:bg-gray-300'
          }`}
          onClick={() => {
            setSelectedMenu(item)
            setIsMenuOpen(false) // Close menu on selection
          }}
        >
          {item}
        </li>
      ))}
    </ul>
  )
}

export default MeAppMenu