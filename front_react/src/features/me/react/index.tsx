import React, { useState } from 'react'
import MeAppMenu from './app-menu'
// TODO: imrove having 2 menus
const Me: React.FC = () => {
  const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false)
  const [selectedMenu, setSelectedMenu] = useState<string>('Menu 1')


  return (
    <div className="h-full w-full flex flex-col md:flex-row">
      {/* Menu Column for big screens*/}
      <div className="bg-gray-200 w-full hidden md:block md:w-1/4 lg:w-1/5 xl:w-1/6 p-4">
        <h2 className="text-lg font-bold mb-4">{/*Menu*/}</h2>
        <MeAppMenu
          selectedMenu={selectedMenu}
          setSelectedMenu={setSelectedMenu}
          setIsMenuOpen={setIsMenuOpen}
        />
      </div>
      {/* Menu Column for small screens */}
      {isMenuOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40"
          onClick={() => setIsMenuOpen(false)} // Close menu when clicking the backdrop
        />
      )}
      <div
        className={`fixed inset-y-0 left-0 max-w-max min-w-1/3 bg-gray-200 z-50 p-4 overflow-y-auto transform transition-transform duration-300 ${isMenuOpen ? 'translate-x-0' : '-translate-x-full'}`}
      >
        <h2 className="text-lg font-bold mb-4 flex justify-between items-center">
          {/*Menu*/}
          <button
            className="text-gray-500 hover:text-gray-700 md:hidden"
            onClick={() => setIsMenuOpen(false)}
            aria-label="Close Menu"
            aria-expanded={isMenuOpen}
          >
            ✕
          </button>
        </h2>
        <MeAppMenu
          selectedMenu={selectedMenu}
          setSelectedMenu={setSelectedMenu}
          setIsMenuOpen={setIsMenuOpen}
        />
      </div>
      {/* Content Column */}
      <div className="flex-1 bg-white p-4">
        <h1 className="text-xl font-bold mb-4">
          <button
            aria-label="Open Menu"
            aria-expanded={isMenuOpen}
            className="text-blue-500 md:hidden w-12 h-full rounded-full hover:bg-gray-200"
            onClick={() => setIsMenuOpen(true)}
          > ☰ </button>
          {selectedMenu}</h1>
        <p>
          This is the content for <strong>{selectedMenu}</strong>. Change the menu item to see different content.
        </p>
      </div>
    </div >
  )
}

export default Me