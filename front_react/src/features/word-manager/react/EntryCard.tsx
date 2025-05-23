import React, { ReactElement, useState } from 'react'
import Entry from '../domain/models/entry'
import Card from '../../../react/widgets/cards/Card'
import PrimaryButton from '../../../react/widgets/buttons/PrimaryButton'
import DangerButton from '../../../react/widgets/buttons/DangerButton'
import Modal from '../../../react/widgets/modal'
import EntryEdit from './EntryEdit'

interface Props {
    entry: Entry
}

const EntryCard: React.FC<Props> = ({ entry }): ReactElement => {
    const [isModalOpen, setIsModalOpen] = useState(false)

    const handleEditClick = () => {
        setIsModalOpen(true)
    }

    const handleCloseModal = () => {
        console.log('Modal closed')
        setIsModalOpen(false)
    }
    return (
        <>
            <Card>
                <div className="flex">
                    {/* First Column */}
                    <div className="flex-shrink-0 flex flex-col space-y-4">
                        {/* Word */}
                        <span className="text-lg font-semibold">{entry.word}</span>
                    </div>

                    {/* Second Column */}
                    <div className="flex-grow">
                        <ul className="list-disc pl-5 space-y-1">
                            {entry.definitions.map((def, i) => (
                                <li key={i} className="text-gray-700">
                                    {def.localized.s}
                                </li>
                            ))}
                        </ul>
                    </div>

                    {/* Third Column */}
                    <div className="flex-shrink-0 flex flex-col md:flex-row md:space-x-2 space-y-2 md:space-y-0">
                        <PrimaryButton onClick={handleEditClick}>
                            Edit
                        </PrimaryButton>
                        <DangerButton>
                            Delete
                        </DangerButton>
                    </div>
                </div>

                {isModalOpen && (
                    <Modal onClose={handleCloseModal}>
                        <EntryEdit entry={entry} onComplete={handleCloseModal} />
                    </Modal>
                )}
            </Card>
        </>)
}

export default EntryCard