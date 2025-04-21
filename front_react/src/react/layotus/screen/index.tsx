import React, { ReactElement, ReactNode } from 'react'

interface Props {
  children: ReactNode
}

const Screen: React.FC<Props> = ({ children }): ReactElement => <div className="h-screen w-screen bg-gray-100 flex items-center justify-center overflow-hidden">
  <div className="h-full w-full bg-white flex items-center justify-center">
    {children}
  </div>
</div>

export default Screen