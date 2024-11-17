import React, { ReactElement, ReactNode } from 'react'

interface Props {
    children: ReactNode
}

const Card: React.FC<Props> = ({ children }): ReactElement => 
        <div className="p-4 bg-white shadow rounded-lg border border-gray-200" >
            {children}
        </div>

export default Card