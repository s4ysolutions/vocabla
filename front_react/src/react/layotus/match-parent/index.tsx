import React, { ReactNode } from 'react'

interface Props {
    children: ReactNode
}

const MatchParent: React.FC<Props> = ({ children }) => (
    <div className="h-full w-full bg-blue-500">{children}</div>
)

export default MatchParent