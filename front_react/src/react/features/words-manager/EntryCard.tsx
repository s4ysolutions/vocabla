import React, {type ReactElement, useState } from 'react'
import Card from '../../widgets/cards/Card.tsx'
import PrimaryButton from '../../widgets/buttons/PrimaryButton.tsx'
import DangerButton from '../../widgets/buttons/DangerButton.tsx'
import Modal from '../../widgets/modal'
import EntryEdit from './EntryEdit.tsx'
import type {Entry} from '../../../domain/Entry.ts';

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
                        <span className="text-lg font-semibold">{entry.word.s}</span>
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
                        <DangerButton onClick>
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
