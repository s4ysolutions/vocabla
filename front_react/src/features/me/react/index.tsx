import React, { useState } from 'react'
import MeAppMenu, { MenuItem, menuItems } from './app-menu'
import WordManager from '../../word-manager/react'
import WordManagerControlPanel from '../../word-manager/react/ControlPanel'
import LearningSettings from '../../learning-settings'
// TODO: imrove having 2 menus
const Me: React.FC = () => {
  const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false)
  const [selectedMenu, setSelectedMenu] = useState<MenuItem>(menuItems[0])


  return (
    <div className="h-full w-full flex flex-col md:flex-row">
      {/* Menu Column for big screens*/}
      <div className="bg-gray-200 w-full hidden md:block md:w-1/4 lg:w-1/5 xl:w-1/6 p-4 border-r border-gray-300">
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
      <div className="flex-1 bg-white p-0 h-full flex flex-col">
        {/* Header Panel */}
        <div className="p-4 bg-gray-200 mb-2 border-b border-gray-300 flex items-center justify-between">
          {/* Open Menu Button */}
          <button
            aria-label="Open Menu"
            aria-expanded={isMenuOpen}
            className="text-blue-500 md:hidden w-12 h-12 rounded-full hover:bg-gray-200 text-4xl flex justify-center"
            onClick={() => setIsMenuOpen(true)}
          >
            ☰
          </button>

          {/* Title */}
          <h1 className="text-xl font-bold flex-1"
            style={{
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
          >{selectedMenu.title}</h1>
          {/* Control Panel */}
          <div className="ml-4">
            {
              {
                'word-manager': <WordManagerControlPanel onFilterChange={() => { }} />,
              }[selectedMenu.id] || null
            }
          </div>
        </div>
        {/* Content Container */}
        <div className="flex-1 overflow-y-hidden">
          {
            {
              'word-manager': <WordManager />,
              'learning-settings': <LearningSettings />,
            }[selectedMenu.id] || <p>Selected a menu item <strong>{selectedMenu.id}</strong>.</p>
          }
        </div>
      </div>
    </div >
  )
}

export default Me